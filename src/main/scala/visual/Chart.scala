// This file is distributed under the BSD 3-clause license.  See file LICENSE.
// Copyright (c) 2015, 2016 Rex Kerr, UCSF, and Calico Labs.

package kse.visual

import scala.math._
import scala.util._
import scala.collection.mutable.{ AnyRefMap => RMap }

import kse.coll._
import kse.maths._
import kse.maths.stats._
import kse.flow._
import kse.eio._

package object chart {
  private[chart] val q = "\""

  def quick(i: InSvg*) {
    val svg = 
      Vector("<html>", "<body>", """<svg width="640" height="480">""", "<g>").map(x => Indent(x)) ++
      i.flatMap(_.inSvg(Xform.flipy(480), None)(DefaultFormatter)).map(x => x.in) ++
      Vector("</g>", "</svg>", "</body>", "</html>").map(x => Indent(x))
    println(svg.mkString("\n"))
    svg.map(_.toString).toFile("test.html".file)
  }
}

package chart {

  trait Shown extends InSvg {
    def style: Style
    def styled: Style = style
    def show(implicit fm: Formatter, mag: Magnification) = 
      fm(if (mag.value closeTo 1) styled else styled.scale(mag.value))
    def showWith(f: Style => Style)(implicit fm: Formatter, mag: Magnification) =
      fm(f(if (mag.value closeTo 1) styled else styled.scale(mag.value)))
  }


  final case class Circ(c: Vc, r: Float, style: Style) extends Shown {
    override def styled = style.shapely
    def inSvg(xform: Xform, mag: Option[Float])(implicit fm: Formatter = DefaultFormatter): Vector[Indent] = {
      if (!r.finite || r <= 0 || !c.finite) return Vector.empty
      val ctr = xform(c)
      val rad = xform.radius(c, Vc(r, 0))
      implicit val myMag = Magnification.from(mag, r, rad)
      if (!rad.finite || rad == 0 || !ctr.finite) return Vector.empty
      Indent.V(
        f"<circle${fm.vquote(ctr, "cx", "cy")}${fm("r", rad)}$show/>"
      )
    }
  }

  final case class Bar(c: Vc, r: Vc, style: Style) extends Shown {
    override def styled = style.shapely
    def inSvg(xform: Xform, mag: Option[Float])(implicit fm: Formatter = DefaultFormatter): Vector[Indent] = {
      if (!r.finite || !c.finite) return Vector.empty
      val s = Vc(r.x, -r.y)
      val ld = xform(c - r)
      val lu = xform(c - s)
      val rd = xform(c + s)
      val ru = xform(c + r)
      Indent.V({
        if ((ld.x closeTo lu.x) && (rd.x closeTo ru.x) && (ld.y closeTo rd.y) && (lu.y closeTo ru.y)) {
          val qc = xform(c)
          val rw = 0.25f*(rd.x - ld.x + ru.x - lu.x).abs
          val rh = 0.25f*(ru.y - rd.y + lu.y - ld.y).abs
          val qr = Vc(rw, rh)
          implicit val myMag = Magnification.from(mag, r.x, r.y, rw, rh)
          f"<rect ${fm.vquote(qc - qr, "x", "y")} ${fm.vquote(qr*2, "width", "height")}$show/>"
        }
        else {
          implicit val myMag = Magnification.from(mag, xform, c)
          f"<polygon points=$q${fm comma ld} ${fm comma rd} ${fm comma ru} ${fm comma lu}$q$show/>"
        }
      })
    }
  }

  final case class DataLine(pts: Array[Long], style: Style) extends Shown {
    override def styled =
      if (style.elements.exists{ case sj: StrokeJoin => true; case _ => false }) style.stroky
      else style.stroky + StrokeJoin(Join.Round)
    def inSvg(xform: Xform, mag: Option[Float])(implicit fm: Formatter): Vector[Indent] = {
      val v = new Array[Long](pts.length)
      var i = 0;
      while (i < v.length) { v(i) = xform(Vc from pts(i)).underlying; i += 1 }
      val sb = new StringBuilder
      sb ++= "<path d=\""
      i = 0;
      while (i < math.min(v.length,1)) { sb ++= "M "; sb ++= fm(Vc from v(i)); i += 1 }
      while (i < math.min(v.length,2)) { sb ++= " L "; sb ++= fm(Vc from v(i)); i += 1 }
      while (i < v.length) { sb += ' '; sb ++= fm(Vc from v(i)); i += 1 }
      implicit val myMag = Magnification.from(mag, xform, pts)
      sb ++= f"$q$show}/>"
      Indent.V(sb.result)
    }
  }

  final case class DataRange(xs: Array[Float], ylos: Array[Float], yhis: Array[Float], style: Style) extends Shown {
    override def styled = style.filly
    def inSvg(xform: Xform, mag: Option[Float])(implicit fm: Formatter): Vector[Indent] = {
      val n = math.min(xs.length, math.min(ylos.length, yhis.length))
      val vs = new Array[Long](2*n)
      var i = 0
      while (i < n) {
        vs(i) = Vc(xs(i), ylos(i)).underlying
        vs(vs.length-1-i) = Vc(xs(i), yhis(i)).underlying
        i += 1
      }
      implicit val myMag = Magnification.from(mag, xform, vs)
      i = 0
      while (i < vs.length) {
        vs(i) = xform(Vc from vs(i)).underlying
        i += 1
      }
      val sb = new StringBuilder
      sb ++= "<path d=\""
      i = 0
      if (i < vs.length) { sb ++= "M "; sb ++= fm(Vc from vs(0)); i += 1 }
      if (vs.length > 1) sb ++= " L"
      while (i < vs.length) { sb += ' '; sb ++= fm(Vc from vs(i)); i += 1 }
      if (n > 0) sb ++= " Z"
      sb ++= f"$q$show/>"
      Indent.V(sb.result)
    }
  }


  /** Note: `hvbias` is (horizontal bar width - vertical bar width)/(horz width + vert width).
    * The wider of the two is drawn at the stroke width; the other, narrower.
    */
  final case class ErrorBarYY(x: Float, lo: Float, hi: Float, across: Float, hvbias: Float, style: Style) extends Shown {
    override def styled = style.stroky
    def inSvg(xform: Xform, mag: Option[Float])(implicit fm: Formatter): Vector[Indent] = {
      implicit val myMag = Magnification.from(mag, xform, Vc(x,lo), Vc(x,hi))
      val l = xform(Vc(x, lo))
      val u = xform(Vc(x, hi))
      val ll = xform(Vc(x-across, lo))
      val lr = xform(Vc(x+across, lo))
      val ul = xform(Vc(x-across, hi))
      val ur = xform(Vc(x+across, hi))
      if (hvbias >= 0.9995)
        Indent.V(f"<path d=${q}M ${fm(l)} L ${fm(u)}${q}$show/>") // Entirely vertical
      else if (hvbias <= -0.9995) 
        Indent.V(f"<path d=${q}M ${fm(ll)} L ${fm(lr)} M ${fm(ul)} L ${fm(ur)}${q}$show/>")  // Entirely horizontal
      else if (hvbias in (-0.005f, 0.005f))
        Indent.V(f"<path d=${q}M ${fm(ll)} L ${fm(lr)} M ${fm(ul)} L ${fm(ur)} M ${fm(l)} L ${fm(u)}${q}$show/>")  // All same thickness
      else {
        // Lines of different thickness
        val mcross = if (hvbias >= 0) 1f else (1 + hvbias)/(1 - hvbias)
        val mriser = if (hvbias <= 0) 1f else (1 - hvbias)/(1 + hvbias)
        Indent.V(
          f"<g${showWith(_.generally)}>",
          f"<path d=${q}M ${fm(ll)} L ${fm(lr)} M ${fm(ul)} L ${fm(ur)}${q}${showWith(_.specifically.scale(mcross))}/>",
          f"<path d=${q}M ${fm(l)} L ${fm(u)}${q}${showWith(_.specifically.scale(mriser))}/>",
          f"</g>"
        )
      }
    }
  }

  /*
  trait Arrowhead {
    def setback: Float
    def stroked(tip: Vc, direction: Vc)(xform: Xform, appear: Appearance)(implicit nf: NumberFormatter, af: AppearanceFormatter): (Float, String)
  }
  final case class LineArrow(angle: Float, length: Float, thickness: Float) extends Arrowhead {
    val phi = angle.abs
    val theta = (math.Pi - phi).toFloat
    val cosx = if (theta < phi) math.cos(theta).toFloat else math.cos(phi).toFloat
    val sinx = if (theta < phi) math.sin(theta).toFloat else math.sin(phi).toFloat
    val flat = phi closeTo (math.Pi/2).toFloat
    val underfilled = 2*thickness < cosx
    val setback = 
      if (flat) 0f
      else if (phi < theta) (cosx/(2*sinx)).toFloat
      else if (underfilled) length*cosx + thickness*sinx
      else length*cosx
    val pointx =
      if (flat) Float.NaN
      else if (phi < theta) thickness/(2*sinx)
      else if (underfilled) setback + (cosx - thickness)/(2*sinx)
      else setback + thickness/(2*sinx)
    val barbx =
      if (flat) thickness/2
      else if (phi < theta) setback + 2*pointx + length*cosx - thickness*sinx*0.5f
      else thickness*sinx/2
    val barby =
      if (flat) 0.5f+length
      else if (phi < theta) 0.5f + length*sinx + thickness*cosx*0.5f
      else if (underfilled) 0.5f + length*sinx - thickness*cosx*0.5f
      else length*sinx + thickness*cosx*0.5f
    def stroked(tip: Vc, direction: Vc)(xform: Xform, appear: Appearance)(implicit nf: NumberFormatter, af: AppearanceFormatter): (Float, String) = {
      val qt = xform(tip)
      val deltadir = if (direction.lenSq < 0.1f*tip.lenSq) direction else direction*(1f/(50*math.max(1e-3f, tip.len.toFloat)))
      val dirx = (xform(tip + deltadir) - qt).hat
      val diry = dirx.ccw
      val w = (af adjust appear).wide.value
      val ap = if (thickness closeTo 1) Plain else new Plainly { override def wide = Q.eval((af adjust appear).wide.value*thickness) }
      val s = w * setback
      val px = w * pointx
      val bx = w * barbx
      val by = w * barby
      val qA = qt - dirx*bx + diry*by
      val qB = qt - dirx*bx - diry*by
      val qC = qt - dirx*px
      val miterfix = if (3.999*sinx < 1) " stroke-miterlimit=\"%d\"".format(math.ceil(1/sinx+1e-3).toInt) else ""
      val ans = 
        if (flat) f"<path d=${q}M ${nf space qA} L ${nf space qB}${q}${af fmt ap}/>"
        else f"<path d=${q}M ${nf space qA} L ${nf space qC} ${nf space qB}${q} stroke-linejoin=${q}miter${q}$miterfix${af fmt ap}/>"
      (s, ans)
    }
  }


  final case class GoTo(from: Q[Vc], to: Q[Vc], indirection: Q[Float], arrow: Q[Arrowhead], appear: Q[Appearance])
  extends ProxyAppear with InSvg {
    override def turnOff = Set(FACE, FILL)
    def inSvg(xform: Xform)(implicit nf: NumberFormatter, af: AppearanceFormatter): Vector[IndentedSvg] = {
      val vf = from.value
      val vt = to.value
      val vi = indirection.value
      val v = vt - vf;
      val ip = vf + v*0.5f - v.ccw*(2f*vi)
      val uf = xform(vf)
      val ut = xform(vt)
      val iq = xform(ip)
      if (arrow.alive) {
        val ar = arrow.value
        val (setback, arrowline) = ar.stroked(vt, (vt - ip).hat)(xform, this)(nf, af)
        val wt = ut - setback*(ut - iq).hat
        val mainline =
          if (indirection.alive)
            f"<path d=${q}M ${nf space uf} Q ${nf space iq} ${nf space wt}${q} fill=${q}none${q}/>"      
          else
            f"<path d=${q}M ${nf space uf} L ${nf space wt}${q} fill=${q}none${q}/>"
        Vector(
          IndentedSvg(f"<g fill=${q}none${q} ${af fmt this}>"),
          IndentedSvg(mainline, 1),
          IndentedSvg(arrowline, 1),
          IndentedSvg("</g>")
        )
      }
      else Vector(IndentedSvg(
        if (indirection.alive)
          f"<path d=${q}M ${nf space uf} Q ${nf space iq} ${nf space ut}${q} fill=${q}none${q}${af fmt this}/>"      
        else
          f"<path d=${q}M ${nf space uf} L ${nf space ut}${q} fill=${q}none${q}${af fmt this}/>"
      ))
    }
  }

  final case class PolyGo(points: Q[Array[Long]], fwdarrow: Q[Arrowhead], bkwarrow: Q[Arrowhead], appear: Q[Appearance])
  extends ProxyAppear with InSvg {
    override def turnOff = Set(FACE, FILL)
    def inSvg(xform: Xform)(implicit nf: NumberFormatter, af: AppearanceFormatter): Vector[IndentedSvg] = {
      val vp = points.value
      if (vp.length < 2) return Vector.empty
      val up = {
        val ans = new Array[Long](vp.length)
        var i = 0
        while (i < vp.length) { ans(i) = xform(Vc from vp(i)).underlying; i += 1 }
        ans
      }
      val fwd = (Vc.from(vp(vp.length-1)) - Vc.from(vp(vp.length-2))).hat
      val bkw = (Vc.from(vp(0)) - Vc.from(vp(1))).hat
      var arrows = List.empty[String]
      if (bkwarrow.alive) {
        val ar = bkwarrow.value
        val (setback, arrowline) = ar.stroked(Vc from vp(0), bkw)(xform, this)(nf, af)
        up(0) = (Vc.from(up(0)) - setback*(Vc.from(up(0)) - Vc.from(up(1))).hat).underlying
        arrows = arrowline :: arrows
      }
      if (fwdarrow.alive) {
        val ar = fwdarrow.value
        val (setback, arrowline) = ar.stroked(Vc from vp(vp.length-1), fwd)(xform, this)(nf, af)
        up(up.length-1) = (Vc.from(up(up.length-1)) - setback*(Vc.from(up(up.length-1)) - Vc.from(up(up.length-2))).hat).underlying
        arrows = arrowline :: arrows
      }
      val line = f"d=${q}M ${nf space Vc.from(up(0))} L ${up.drop(1).map(l => nf space Vc.from(l)).mkString(" ")}${q}"

      Vector(
        IndentedSvg(f"<g fill=${q}none${q} ${af fmt this}>"),
        IndentedSvg(f"<path fill=${q}none${q} $line/>", 1)
      ) ++
      arrows.map(s => IndentedSvg(s, 1)) ++
      Vector(IndentedSvg("</g>"))
    }
  }

  final case class Letters(pt: Q[Vc], text: Q[String], height: Q[Float], appear: Q[Appearance])
  extends ProxyAppear with InSvg {
    def inSvg(xform: Xform)(implicit nf: NumberFormatter, af: AppearanceFormatter): Vector[IndentedSvg] = {
      val p = pt.value
      val vl = xform(p - Vc(0, 0.5f*height.value))
      val vh = xform(p + Vc(0, 0.5f*height.value))
      if (vl.x closeTo vh.x) {
        val size = (vh.y - vl.y).abs
        Vector(IndentedSvg(
          f"<text x=$q${nf fmt vl.x}$q y=$q${nf fmt vl.y}$q font-size=$q${nf fmt size}$q text-anchor=${"\"middle\""}${af fmt this}>${text.value}</text>"
        ))        
      }
      else ???
    }
  }

  final case class TickMarks(
    origin: Q[Vc], axis: Q[Vc],
    left: Q[Float], right: Q[Float], values: Q[Array[Float]],
    labelSize: Q[Float], labelAt: Q[Float], labelRot: Q[Float], labels: Q[Array[String]],
    mainSize: Q[Float], mainAt: Q[Float], mainRot: Q[Float], mainText: Q[String],
    appear: Q[Appearance], textappear: Q[Appearance] = Q empty Plain
  )
  extends ProxyAppear with InSvg {
    override def turnOff = Set(FILL)
    def inSvg(xform: Xform)(implicit nf: NumberFormatter, af: AppearanceFormatter): Vector[IndentedSvg] = {
      val va = axis.value.hat
      val vb = va.ccw
      val w = (af adjust appear.value).wide.value
      val ul = left.value * w
      val ur = right.value * w
      val vo = origin.value
      val vv = values.value
      val pts = vv.map{ x =>
        val vc = vo + x*va
        val uc = xform(vc)
        val orth = (xform(vc+vb*1e-2f) - uc).hat
        val l = uc + orth*ul
        val r = uc + orth*ur
        f"M ${nf space l} L ${nf space r}"
      }
      val uls = w * (if (labelSize.alive) labelSize.value else 10f)
      val ula =
        if (labelAt.alive) labelAt.value * w
        else if ((va * Vc(1,0)).abs > (va * Vc(0,1)).abs) math.min(ul,ur) - (ul-ur).abs - uls/2
        else math.max(ul, ur) + (ul-ur).abs + uls/2
      val vlr = if (labelRot.alive) labelRot.value else 0
      val lb =
        if (labels.alive) {
          val hanchor = " text-anchor=\"middle\""
          val vanchor = " dominant-baseline=\"middle\""
          val lines =
            (vv zip labels.value).collect{ case (x, l) if l.nonEmpty =>
              val vc = vo + x*va
              val uc = xform(vc)
              val orth = (xform(vc+vb*1e-2f) - uc).hat
              val u = uc + orth * ula
              // Need vanchor here to keep Firefox happy, sadly.
              f"<text$vanchor x=$q${nf fmt u.x}$q y=$q${nf fmt u.y}$q>$l</text>"
            }
          if (lines.isEmpty) Vector.empty
          else {
            val app = if (textappear.alive) textappear.value else Filled(appear.value.stroke.value)
            val common = f"font-size=$q${nf fmt uls}$q$hanchor$vanchor${af fmt app}"
            IndentedSvg(f"<g $common>") +: lines.toVector.map(l => IndentedSvg(l, 1)) :+ IndentedSvg("</g>")
          }
        }
        else Vector.empty
      val tickpath = f"<path d=${q}${pts.mkString(" ")}${q} fill=${q}none${q}${af fmt this}/>"
      if (lb.isEmpty) Vector(IndentedSvg(tickpath))
      else Vector(IndentedSvg("<g>"), IndentedSvg(tickpath, 1)) ++ lb.map(x => x.copy(level = x.level+1)) :+ IndentedSvg("</g>")
    }
  }

  sealed class MuGroup(var elements: Vector[InSvg]) extends InSvg {
    def +=(i: InSvg): this.type = { elements = elements :+ i; this }
    def inSvg(xform: Xform)(implicit nf: NumberFormatter, af: AppearanceFormatter) = {
      elements.flatMap(i => i.inSvg(xform)(nf, af)) match {
        case vs => IndentedSvg("<g>") +: vs.map(x => x.copy(level = x.level + 1)) :+ IndentedSvg("</g>")
      }
    }
  }
  object MuGroup {
    def apply(elements: InSvg*) = new MuGroup(elements.toVector)
  }

  final class Origin(origin: Q[Vc], scaling: Q[Vc], theElements: Vector[InSvg]) extends MuGroup(theElements) {
    private[this] def undo: Xform = Xform.shiftscale(origin.value, scaling.value).inverted
    override def inSvg(xform: Xform)(implicit nf: NumberFormatter, af: AppearanceFormatter) =
      super.inSvg(undo andThen xform)(nf, af)
  }
  object Origin {
    def apply(origin: Q[Vc], scaling: Q[Vc], elements: InSvg*) = new Origin(origin, scaling, elements.toVector)
  }

  final class ZoomLines(scaling: Q[Float], theElements: Vector[InSvg]) extends MuGroup(theElements) {
    override def inSvg(xform: Xform)(implicit nf: NumberFormatter, af: AppearanceFormatter) =
      super.inSvg(xform)(nf, ZoomingFormatter(scaling.value, af))
  }
  object ZoomLines {
    def apply(scaling: Q[Float], elements: InSvg*) = new ZoomLines(scaling, elements.toVector)
    def apply(scaling: Float, elements: InSvg*) = new ZoomLines(Q(scaling), elements.toVector)
  }
  */
}
