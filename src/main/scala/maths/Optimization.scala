// This file is distributed under the BSD 3-clause license.  See file LICENSE.
// Copyright (c) 2011-16 Rex Kerr

package kse.maths
package optimization

import scala.reflect.ClassTag
import scala.math._

import kse.coll.Copy

object DataShepherd {
  def ensureFinite(xs: Array[Double]): Array[Double] = {
    // Keep Double and Float implementations perfectly in sync!  YOU HAVE TO DO THIS MANUALLY!!
    var i, n = 0
    while (i < xs.length) { if (xs(i).finite) n += 1; i += 1 }
    if (i == n) xs
    else {
      val fxs = new Array[Double](n)
      i = 0
      var j = 0
      while (j < fxs.length && i < xs.length) {
        val xi = xs(i)
        if (xi.finite) {
          fxs(j) = xi
          j += 1
        }
        i += 1
      }
      fxs
    }
  }
  def ensureFinite(xs: Array[Double], ys: Array[Double]): (Array[Double], Array[Double]) = {
    // Keep Double and Float implementations perfectly in sync!  YOU HAVE TO DO THIS MANUALLY!!
    var i, n = 0
    while (i < xs.length && i < ys.length) { if (xs(i).finite && ys(i).finite) n += 1; i += 1 }
    if (n == xs.length && n == ys.length) (xs, ys)
    else {
      val fxs, fys = new Array[Double](n)
      i = 0
      var j = 0
      while (j < fxs.length && i < xs.length && i < ys.length) {
        val xi = xs(i)
        val yi = ys(i)
        if (xi.finite && yi.finite) {
          fxs(j) = xi
          fys(j) = yi
          j += 1
        }
        i += 1
      }
      (fxs, fys)
    }
  }
  def ensureFinite(xs: Array[Double], ys: Array[Double], zs: Array[Double]): (Array[Double], Array[Double], Array[Double]) = {
    // Keep Double and Float implementations perfectly in sync!  YOU HAVE TO DO THIS MANUALLY!!
    var i, n = 0
    while (i < xs.length && i < ys.length && i < zs.length) { if (xs(i).finite && ys(i).finite && zs(i).finite) n += 1; i += 1 }
    if (n == xs.length && n == ys.length && n == zs.length) (xs, ys, zs)
    else {
      val fxs, fys, fzs = new Array[Double](n)
      i = 0
      var j = 0
      while (j < fxs.length && i < xs.length && i < ys.length && i < zs.length) {
        val xi = xs(i)
        val yi = ys(i)
        val zi = zs(i)
        if (xi.finite && yi.finite & zi.finite) {
          fxs(j) = xi
          fys(j) = yi
          fzs(j) = zi
          j += 1
        }
        i += 1
      }
      (fxs, fys, fzs)
    }
  }
  def ensureFinite(xs: Array[Float]): Array[Float] = {
    // Keep Double and Float implementations perfectly in sync!  YOU HAVE TO DO THIS MANUALLY!!
    var i, n = 0
    while (i < xs.length) { if (xs(i).finite) n += 1; i += 1 }
    if (i == n) xs
    else {
      val fxs = new Array[Float](n)
      i = 0
      var j = 0
      while (j < fxs.length && i < xs.length) {
        val xi = xs(i)
        if (xi.finite) {
          fxs(j) = xi
          j += 1
        }
        i += 1
      }
      fxs
    }
  }
  def ensureFinite(xs: Array[Float], ys: Array[Float]): (Array[Float], Array[Float]) = {
    // Keep Double and Float implementations perfectly in sync!  YOU HAVE TO DO THIS MANUALLY!!
    // Also keep this and Array[Long] implementations perfectly in sync!  YOU HAVE TO DO THIS MANUALLY!!
    var i, n = 0
    while (i < xs.length && i < ys.length) { if (xs(i).finite && ys(i).finite) n += 1; i += 1 }
    if (n == xs.length && n == ys.length) (xs, ys)
    else {
      val fxs, fys = new Array[Float](n)
      i = 0
      var j = 0
      while (j < fxs.length && i < xs.length && i < ys.length) {
        val xi = xs(i)
        val yi = ys(i)
        if (xi.finite && yi.finite) {
          fxs(j) = xi
          fys(j) = yi
          j += 1
        }
        i += 1
      }
      (fxs, fys)
    }
  }
  def ensureFinite(xs: Array[Float], ys: Array[Float], zs: Array[Float]): (Array[Float], Array[Float], Array[Float]) = {
    // Keep Double and Float implementations perfectly in sync!  YOU HAVE TO DO THIS MANUALLY!!
    var i, n = 0
    while (i < xs.length && i < ys.length && i < zs.length) { if (xs(i).finite && ys(i).finite && zs(i).finite) n += 1; i += 1 }
    if (n == xs.length && n == ys.length && n == zs.length) (xs, ys, zs)
    else {
      val fxs, fys, fzs = new Array[Float](n)
      i = 0
      var j = 0
      while (j < fxs.length && i < xs.length && i < ys.length && i < zs.length) {
        val xi = xs(i)
        val yi = ys(i)
        val zi = zs(i)
        if (xi.finite && yi.finite & zi.finite) {
          fxs(j) = xi
          fys(j) = yi
          fzs(j) = zi
          j += 1
        }
        i += 1
      }
      (fxs, fys, fzs)
    }
  }
  def ensureFinite(xys: Array[Long]): Array[Long] = {
    // Keep this and Float,Float implementations perfectly in sync!  YOU HAVE TO DO THIS MANUALLY!!
    var i, n = 0
    while (i < xys.length) { if (Vc.from(xys(i)).finite) n += 1; i += 1 }
    if (i == n) xys
    else {
      val fxys = new Array[Long](n)
      i = 0
      var j = 0
      while (j < fxys.length && i < xys.length) {
        val xyi = xys(i)
        if (Vc.from(xyi).finite) {
          fxys(j) = xyi
          j += 1
        }
        i += 1
      }
      fxys
    }
  }

  private def ensureCopy(xs: Array[Double], oxs: Array[Double]) =
    if (xs ne oxs) xs else java.util.Arrays.copyOf(oxs, oxs.length)
  private def ensureCopy(xs: Array[Float], oxs: Array[Float]) =
    if (xs ne oxs) xs else java.util.Arrays.copyOf(oxs, oxs.length)
  private def ensureCopy(xys: Array[Long], oxys: Array[Long]) =
    if (xys ne oxys) xys else java.util.Arrays.copyOf(oxys, oxys.length)

  def copyFinite(xs: Array[Double]): Array[Double] = ensureCopy(ensureFinite(xs), xs)
  def copyFinite(xs: Array[Double], ys: Array[Double]): (Array[Double], Array[Double]) = { 
    val (fxs, fys) = ensureFinite(xs, ys)
    (ensureCopy(fxs, xs), ensureCopy(fys, ys))
  }
  def copyFinite(xs: Array[Double], ys: Array[Double], zs: Array[Double]): (Array[Double], Array[Double], Array[Double]) = {
    val (fxs, fys, fzs) = ensureFinite(xs, ys, zs)
    (ensureCopy(fxs, xs), ensureCopy(fys, ys), ensureCopy(fzs, zs))
  }
  def copyFinite(xs: Array[Float]): Array[Float] = ensureCopy(ensureFinite(xs), xs)
  def copyFinite(xs: Array[Float], ys: Array[Float]): (Array[Float], Array[Float]) = { 
    val (fxs, fys) = ensureFinite(xs, ys)
    (ensureCopy(fxs, xs), ensureCopy(fys, ys))
  }
  def copyFinite(xs: Array[Float], ys: Array[Float], zs: Array[Float]): (Array[Float], Array[Float], Array[Float]) = {
    val (fxs, fys, fzs) = ensureFinite(xs, ys, zs)
    (ensureCopy(fxs, xs), ensureCopy(fys, ys), ensureCopy(fzs, zs))
  }
  def copyFinite(xys: Array[Long]): Array[Long] = ensureCopy(ensureFinite(xys), xys)

  def bind(xs: Array[Float], ys: Array[Float]): Array[Long] = {
    val n = math.min(xs.length, ys.length)
    val xys = new Array[Long](n)
    var i = 0
    while (i < n) {
      xys(i) = Vc(xs(i), ys(i)).underlying
      i += 1
    }
    xys
  }
  def bindAsFloat(xs: Array[Double], ys: Array[Double]): Array[Long] = {
    val n = math.min(xs.length, ys.length)
    val xys = new Array[Long](n)
    var i = 0
    while (i < n) {
      xys(i) = Vc.from(xs(i), ys(i)).underlying
      i += 1
    }
    xys
  }
  def unbind(xys: Array[Long]): (Array[Float], Array[Float]) = {
    val xs, ys = new Array[Float](xys.length)
    var i = 0
    while (i < xys.length) {
      val xyi = Vc from xys(i)
      xs(i) = xyi.x
      ys(i) = xyi.y
      i += 1
    }
    (xs, ys)
  }
  def unbindAsDouble(xys: Array[Long]): (Array[Double], Array[Double]) = {
    val xs, ys = new Array[Double](xys.length)
    var i = 0
    while (i < xys.length) {
      val xyi = Vc from xys(i)
      xs(i) = xyi.x
      ys(i) = xyi.y
      i += 1
    }
    (xs, ys)
  }
  def bindFinite(xs: Array[Float], ys: Array[Float]): Array[Long] = {
    val n = math.min(xs.length, ys.length)
    val xys = new Array[Long](n)
    var i, j = 0
    while (i < n) {
      val xi = xs(i)
      val yi = ys(i)
      if (xi.finite && yi.finite) {
        xys(j) = Vc(xi, yi).underlying
        j += 1
      }
      i += 1
    }
    if (j == n) xys else java.util.Arrays.copyOf(xys, xys.length)
  }
  def bindFiniteAsFloat(xs: Array[Double], ys: Array[Double]): Array[Long] = {
    val n = math.min(xs.length, ys.length)
    val xys = new Array[Long](n)
    var i, j = 0
    while (i < n) {
      val xi = xs(i).toFloat
      val yi = ys(i).toFloat
      if (xi.finite && yi.finite) {
        xys(j) = Vc(xi, yi).underlying
        j += 1
      }
      i += 1
    }
    if (j == n) xys else java.util.Arrays.copyOf(xys, j)
  }
  def unbindFinite(xys: Array[Long]): (Array[Float], Array[Float]) = {
    val n = xys.length
    val xs, ys = new Array[Float](n)
    var i, j = 0
    while (i < n) {
      val xyi = Vc from xys(i)
      if (xyi.finite) {
        xs(j) = xyi.x
        ys(j) = xyi.y
        j += 1
      }
      i += 1
    }
    if (j == n) (xs, ys)
    else (java.util.Arrays.copyOf(xs, j), java.util.Arrays.copyOf(ys, j))
  }
  def unbindFiniteAsDouble(xys: Array[Long]): (Array[Double], Array[Double]) = {
    val n = xys.length
    val xs, ys = new Array[Double](n)
    var i, j = 0
    while (i < n) {
      val xyi = Vc from xys(i)
      if (xyi.finite) {
        xs(j) = xyi.x
        ys(j) = xyi.y
        j += 1
      }
      i += 1
    }
    if (j == n) (xs, ys)
    else (java.util.Arrays.copyOf(xs, j), java.util.Arrays.copyOf(ys, j))
  }

  def clobberHugeOutliers(xs: Array[Double], sdsep: Double, sdabs: Double): Boolean = {
    val ixs = Array.range(0, xs.length).sortBy{ i =>
      val xi = xs(i)
      if (xi.finite) xi else Double.PositiveInfinity
    }
    var j0 = 0
    var jN = ixs.length
    while (jN > 0 && !xs(ixs(jN-1)).finite) jN -= 1
    if (jN - j0 > 20) {
      // Enough points so we can entertain the idea that there are outliers
      val e = new stats.EstM
      var j = j0
      while (j < jN) { e += xs(ixs(j)); j += 1 }
      var changed = true
      while (changed && jN - j0 > 20) {
        val ecutL = e.clone
        var kl = j0
        var saneL = true
        var xx = xs(ixs(kl))
        while (kl < j0+10 && saneL) {
          val x = xx
          xx = xs(ixs(kl+1))
          ecutL -= x
          if ((x - xx).sq > sdsep.sq*ecutL.variance || (x - ecutL.value).sq > sdabs.sq*ecutL.variance) saneL = false
          kl += 1
        }
        val ecutR = e.clone
        var kr = jN-1
        var saneR = true
        xx = xs(ixs(kr))
        while (kr >= jN-10 && saneR) {
          val x = xx
          xx = xs(ixs(kr-1))
          ecutR -= x
          if ((x - xx).sq > sdsep.sq*ecutR.variance || (x - ecutR.value).sq > sdabs.sq*ecutR.variance) saneR = false
          kr -= 1
        }
        if (!saneL) j0 = kl
        if (!saneR) jN = kr+1
        changed = !(saneL && saneR)
      }
    }
    if ((jN - j0)*1.05 <= xs.length) { j0 = 0; jN = xs.length }
    if (j0 > 0) { var j = 0; while (j < j0) { xs(ixs(j)) = Double.NaN; j += 1 } }
    if (jN < xs.length) { var j = xs.length - 1; while (j >= jN) { xs(ixs(j)) = Double.NaN; j -= 1 } }
    !(j0 == 0 && jN == xs.length)
  }
  def clobberHugeOutliers(xs: Array[Float], sdsep: Double, sdabs: Double): Boolean = {
    val big = xs.toDoubles
    val ans = clobberHugeOutliers(big, sdsep, sdabs)
    if (ans) {
      var i = 0
      while (i < xs.length) { if (big(i).nan) xs(i) = Float.NaN; i += 1 }
    }
    ans
  }
  def clobberHugeOutliers(xys: Array[Long], sdsep: Double, sdabs: Double): Boolean = {
    val (bigx, bigy) = unbindAsDouble(xys)
    val ansx = clobberHugeOutliers(bigx, sdsep, sdabs)
    val ansy = clobberHugeOutliers(bigy, sdsep, sdabs)
    val ans = ansx || ansy
    if (ans) {
      var i = 0
      val lNaN = Vc(Float.NaN, Float.NaN).underlying
      while (i < xys.length) { if (bigx(i).nan || bigy(i).nan) xys(i) = lNaN; i += 1 }
    }
    ans
  }
  def clobberHugeOutliers(xs: Array[Double]): Boolean = clobberHugeOutliers(xs, 2, 10)
  def clobberHugeOutliers(xs: Array[Float]): Boolean = clobberHugeOutliers(xs, 2, 10)
  def clobberHugeOutliers(xys: Array[Long]): Boolean = clobberHugeOutliers(xys, 2, 10)

  def pearson(xys: Array[Long]): Double = {
    var sx, sxx, sy, syy, sxy = 0.0
    var n = 0
    var i = 0
    while (i < xys.length) {
      val v = Vc from xys(i)
      if (v.finite) {
        val x = v.x.toDouble
        val y = v.y.toDouble
        sx += x
        sxx += x*x
        sy += y
        syy += y*y
        sxy += x*y
        n += 1
      }
      i += 1
    }
    if (n < 2) 0
    else {
      val Vx = sxx/n - (sx/n).sq
      val Vy = syy/n - (sy/n).sq
      if ((Vx closeTo 0) || (Vy closeTo 0)) 0
      else math.max(-1, math.min(1, ((sxy/n) - (sx/n)*(sy/n))/math.sqrt(Vx*Vy) ))
    }
  }
  def pearson(xs: Array[Float], ys: Array[Float]): Double = pearson(bind(xs, ys))
  def pearson(xs: Array[Double], ys: Array[Double]): Double = pearson(bindAsFloat(xs, ys))
}


abstract class Approximator extends Copy[Approximator] {
  def name: String
  def copy: Approximator
  def normalize: this.type = this
  protected def prettyArgs(inName: String, figs: Int): String
  def pretty(inName: String, outName: String, figs: Int, argName: String = ""): String = 
    f"$outName(${if (argName.isEmpty) inName else argName}) = ${prettyArgs(inName, figs)}"
  val parameters: Array[Double]
  def apply(datum: Double): Double
  def apply(data: Array[Double]): Array[Double] = computeInto(data, null)
  def apply(data: Array[Float]): Array[Float] = computeInto(data.toDoubles, null).toFloats
  def computeInPlace(data: Array[Double]) {
    var i = 0
    while (i < data.length) { data(i) = apply(data(i)); i += 1 }
  }
  def computeInto(data: Array[Double], target: Array[Double]): Array[Double] = {
    val a = if ((target eq null) || target.length < data.length) new Array[Double](data.length) else target
    var i = 0
    while (i < a.length) { a(i) = apply(data(i)); i += 1 }
    a
  }
}


trait ApproximatorCompanion[+App <: Approximator] {
  def guess(ts: Array[Double], xs: Array[Double], finitize: Boolean): List[App]
}

object Approximator {
  private[this] def findCleanLeftRightSlopes(
    ts: Array[Double], xs: Array[Double], finitize: Boolean = true
  ): (fits.FitTX, fits.FitTX, Array[Double], Array[Double], Int) = {
    val (fts, fxs) = if (finitize) DataShepherd.ensureFinite(ts, xs) else (ts, xs)
    val n = math.min(fts.length, fxs.length)
    if (n <= 0) return (null, null, fts, fxs, 0)
    val left, right = new fits.FitTX
    var i = 0
    var j = n-1
    var winner = 0
    while (i < j && winner.abs < 6) {
      left += (fts(i), fxs(i))
      right += (fts(j), fxs(j))
      if (i > 4) {
        val lb = left.betaX.abs
        val rb = right.betaX.abs
        val call = if (lb > rb*1.2) -1 else if (rb > lb*1.2) 1 else 0
        if (call == 0) winner = 0
        else if (winner == 0) winner = call
        else if (winner*call < 0) winner = call
        else winner += call
      }
      i += 1
      j -= 1
    }
    (left, right, fts, fxs, winner)
  }

  final class Constant(x0: Double) extends Approximator with Copy[Constant] {
    def name = "Constant"
    def prettyArgs(inName: String, figs: Int) = Constant.prettyValue(parameters(0), figs)
    val parameters = Array(x0)
    def copy = new Constant(parameters(0))
    def apply(t: Double) = parameters(0)
    override def toString = f"x = ${parameters(0)}"
  }
  object Constant extends ApproximatorCompanion[Constant] {
    def prettyValue(value: Double, figs: Int) = {
      val direct = ("%."+max(0, figs) + "e").format(value).toDouble.toString
      var i = direct.length - 1
      while (i > 0 && direct.charAt(i) == '0') i -= 1
      if (direct.charAt(i) == '.') direct.substring(0, i) else direct
    }
    def guess(ts: Array[Double], xs: Array[Double], finitize: Boolean = true): List[Constant] = {
      val e = new stats.EstM
      var i = 0
      while (i < xs.length) { e += xs(i); i += 1 }
      if (e.n > 0) (new Constant(e.value)) :: Nil else Nil
    }
  }


  final class Affine(x0: Double, slope: Double) extends Approximator with Copy[Affine] {
    def name = "Affine"
    def prettyArgs(inName: String, figs: Int) =
      Constant.prettyValue(parameters(0), figs) +
      (if (parameters(1) < 0) " - " + Constant.prettyValue(-parameters(1), figs) else " + " + Constant.prettyValue(parameters(1), figs)) +
      "*" + inName
    val parameters = Array(x0, slope)
    def copy = new Affine(parameters(0), parameters(1))
    def apply(t: Double) = parameters(0) + parameters(1)*t
    override def toString = f"x = ${parameters(0)} + ${parameters(1)}*t"
  }
  object Affine extends ApproximatorCompanion[Affine] {
    def guess(ts: Array[Double], xs: Array[Double], finitize: Boolean = true): List[Affine] = {
      val n = math.min(ts.length, xs.length)
      if (n <= 0) return Nil
      var ftx = fits.FitTX(ts, xs, 0, math.min(ts.length, xs.length))
      if (finitize && !ftx(0).finite) {
        val (nts, nxs) = DataShepherd.copyFinite(ts, xs)
        guess(nts, nxs, false)
      }
      else new Affine(ftx(0), ftx.betaX) :: Nil
    }
  }

  final class Quadratic(a0: Double, a1: Double, a2: Double) extends Approximator with Copy[Quadratic] {
    def name = "Quadratic"
    def prettyArgs(inName: String, figs: Int) =
      Constant.prettyValue(parameters(0), figs) +
      (if (parameters(1) < 0) " - " + Constant.prettyValue(-parameters(1), figs) else " + " + Constant.prettyValue(parameters(1), figs)) +
      "*" + inName +
      (if (parameters(2) < 0) " - " + Constant.prettyValue(-parameters(2), figs) else " + " + Constant.prettyValue(parameters(2), figs)) +
      "*" + inName + "^2"
    val parameters = Array(a0, a1, a2)
    def copy = new Quadratic(parameters(0), parameters(1), parameters(2))
    def apply(t: Double) = { parameters(0) + parameters(1)*t + parameters(2)*t*t }
    override def toString = f"x = ${parameters(0)} + ${parameters(1)}*t + ${parameters(2)}*t^2"
  }
  object Quadratic extends ApproximatorCompanion[Quadratic] {
    def guess(ts: Array[Double], xs: Array[Double], finitize: Boolean = true): List[Quadratic] = {
      val (fts, fxs) = if (finitize) DataShepherd.ensureFinite(ts, xs) else (ts, xs)
      if (fts.length == 0) Nil
      else if (fts.length == 1) (new Quadratic(fxs(0), 0, 0)) :: Nil
      else if (fts.length == 2) Affine.guess(fts, fxs, false).map{ a => new Quadratic(a.parameters(0), a.parameters(1), 0) }
      else {
        val elt, elx, ect, ecx, ert, erx = new stats.EstM
        var i = 0
        while (i*3 < fts.length) {
          elt += fts(i)
          elx += fxs(i)
          i += 1
        }
        while ((i*3)/2 < fts.length) {
          ect += fts(i)
          ecx += fxs(i)
          i += 1
        }
        while (i < fts.length) {
          ert += fts(i)
          erx += fxs(i)
          i += 1
        }
        val tl = elt.value
        val tc = ect.value
        val tr = ert.value
        val xl = elx.value
        val xc = ecx.value
        val xr = erx.value
        if ((xc closeTo xl) && (xc closeTo xr)) (new Quadratic(xc, 0, 0)) :: Nil
        else if ((xc-xl)/(tc-tl) closeTo (xr-xc)/(tr-tc)) {
          val slope = (xr -xl)/(tr - tl)
          // Must have xc = a0 + tc*slope
          if (slope closeTo 0) (new Quadratic(xc, 0, 0)) :: Nil
          else (new Quadratic(xc - tc*slope, slope, 0)) :: Nil
        }
        else {
          // Nondegenerate
          // xl = a0 + a1*tl + a2*tl^2
          // xr = a0 + a1*tr + a2*tr^2
          // xc - xl = a1*(tc - tl) + a2*(tc - tl)*(tc + tl)
          // (xc - xl)*(tr - tc) - (xr - xc)*(tc - tl) = a2*((tc - tl)*(tc + tl)*(tr - tc) - (tr - tc)*(tr + tc)*(tc - tl))
          val trc = tr - tc
          val xrc = xr - xc
          val tcl = tc - tl
          val xcl = xc - xl
          val a2 = (xcl*trc - xrc*tcl)/(trc*tcl*(tl - tr))
          // Now that we have a2, use (xr - xl) = a1*(tr - tl) + a2*(tr - tl)*(tr + tl)
          val a1 = ((xr - xl) - a2*(tr-tl)*(tr+tl))/(tr - tl)
          // Now that we have a1, just use center point to find offset
          val a0 = xc - a1*tc - a2*tc*tc
          (new Quadratic(a0, a1, a2)) :: Nil
        }
      }
    }
  }

  final class Exponential(offset: Double, height: Double, slope: Double) extends Approximator with Copy[Exponential] {
    def name = "Exponential"
    override def normalize = { if (parameters(1) closeTo 0) { parameters(1) = 0; parameters(2) = 0 }; this }
    def prettyArgs(inName: String, figs: Int) = {
      Constant.prettyValue(parameters(0), figs) +
      (if (parameters(1) < 0) " - " + Constant.prettyValue(-parameters(1), figs) else " + " + Constant.prettyValue(parameters(1), figs)) +
      "*e^(" + (if (parameters(1) == 0) "0" else Constant.prettyValue(parameters(2)/parameters(1), figs)) + "*" + inName + ")"
    }
    // Equation is x = offset + height*exp((slope/height)*t), so that dx/dt(0) = slope
    val parameters = Array(offset, height, slope)
    def copy = new Exponential(parameters(0), parameters(1), parameters(2))
    def apply(t: Double) = { val h = parameters(1); parameters(0) + (if (h != 0) h*exp(parameters(2)*t/h) else 0.0) }
    override def toString = f"x = ${parameters(0)} + ${parameters(1)}*e^(${parameters(2)/parameters(1)}*t)"
  }
  object Exponential extends ApproximatorCompanion[Exponential] {
    private[this] def fromSlopes(ours: fits.FitTX, theirs: fits.FitTX, correctionLimit: Double = 0.9) = {
        // Exponential of form x = x0 + a*exp((b/a)*t) so that dx/dt(0) = b
        val tc = ours.meanT    // time at which we have a slope estimate
        val xc = ours.meanX    // height at that time
        val bc = ours.betaX    // slope
        // If slopes change, just take difference as contribution of exponential
        // If both slopes have same sign, height is proportional to slope so we can correct
        val nonconst = 
          if (signum(xc - theirs.meanX)*signum(tc - theirs.meanT) == signum(bc)) // No weird wiggles
            (xc - theirs.meanX)/(1 - (if (theirs.betaX * bc > 0) min(correctionLimit, theirs.betaX/bc) else 0))
          else xc - theirs.meanX
        // Scale factor due to time not being zero
        val scale = exp(bc*tc / nonconst)
        val height = nonconst / scale
        new Exponential(xc - nonconst, height, bc/scale)
    }
    def guess(ts: Array[Double], xs: Array[Double], finitize: Boolean = true): List[Exponential] = {
      val (left, right, _, _, winner) = findCleanLeftRightSlopes(ts, xs, finitize)
      if ((left eq null) || (right eq null)) return Nil
      if (winner.abs < 6) {
        // Can't reliably tell whether it's growing or shrinking, so we'd better return both
        if (signum(left.betaX) != signum(right.betaX)) fromSlopes(left, right, 0) :: fromSlopes(right, left, 0) :: Nil
        else {
          val cx = (left.meanX + right.meanX)/2
          val ct = (left.meanT + right.meanT)/2
          val dt = right.meanT - left.meanT
          val dx = right.meanX - left.meanX
          val cs = dx/dt
          val big = exp(4)
          // Have cx = x0 + a*exp((b/a)*ct)
          // Could choose a same sign as dx or opposite sign
          val rates = (dt :: (-dt) :: Nil).map(t => 1/(big*t))  // Change of dt produces change of about 1/big
          rates.map{ r =>
            // Pretty flat
            val scale = exp(r*ct)
            val height = big*dx/scale * signum(r)
            val offset = cx - height*scale
            new Exponential(offset, height, height*r)
          } ::: List(
            new Exponential(left.meanX, dx, cs),
            new Exponential(right.meanX, -dx, -cs)
          )
        }
      }
      else {
        // One side is clearly steeper than the other.  Use it.
        if (winner < 0) fromSlopes(left, right) :: Nil   // Decaying exponential, use left edge slope
        else            fromSlopes(right, left) :: Nil   // Rising exponential, use right edge slope
      }
    }
  }

  final class Biexponential(offset: Double, height: Double, slope: Double, slowHeight: Double, slowSlope: Double) extends Approximator with Copy[Biexponential] {
    def name = "Biexponential"
    override def normalize = {
      if (parameters(1) closeTo 0) { parameters(1) = 0; parameters(2) = 0 }
      if (parameters(3) closeTo 0) { parameters(3) = 0; parameters(4) = 0 }
      if (parameters(3) != 0) {
        if (parameters(1) == 0 || (parameters(4)/parameters(3)).abs > (parameters(2)/parameters(1)).abs) {
          val h = parameters(1)
          val s = parameters(2)
          parameters(1) = parameters(3)
          parameters(2) = parameters(4)
          parameters(3) = h
          parameters(4) = s
        }
      }
      this
    }
    def prettyArgs(inName: String, figs: Int) = {
      Constant.prettyValue(parameters(0), figs) +
      (if (parameters(1) < 0) " - " + Constant.prettyValue(-parameters(1), figs) else " + " + Constant.prettyValue(parameters(1), figs)) +
      "*e^(" + (if (parameters(1) == 0) "0" else Constant.prettyValue(parameters(2)/parameters(1), figs)) + "*" + inName + ")" +
      (if (parameters(3) < 0) " - " + Constant.prettyValue(-parameters(3), figs) else " + " + Constant.prettyValue(parameters(3), figs)) +
      "*e^(" + (if (parameters(3) == 0) "0" else Constant.prettyValue(parameters(4)/parameters(3), figs)) + "*" + inName + ")"
    }
    val parameters = Array(offset, height, slope, slowHeight, slowSlope)
    def copy = new Biexponential(parameters(0), parameters(1), parameters(2), parameters(3), parameters(4))
    def apply(t: Double) = { 
      val h = parameters(1)
      val sh = parameters(3)
      parameters(0) + (if (h != 0) h*exp(parameters(2)*t/h) else 0.0) + (if (sh != 0) sh*exp(parameters(4)*t/sh) else 0.0)
    }
    override def toString = f"x = ${parameters(0)} + ${parameters(1)}*e^(${parameters(2)/parameters(1)}*t) + ${parameters(3)}*e^(${parameters(4)/parameters(3)}*t)"
  }
  object Biexponential extends ApproximatorCompanion[Biexponential] {
    def guess(ts: Array[Double], xs: Array[Double], finitize: Boolean = true): List[Biexponential] = {
      Exponential.guess(ts, xs, finitize).map{ case ape =>
        new Biexponential(ape.parameters(0), ape.parameters(1)*0.9, ape.parameters(2)*0.9, ape.parameters(1)*0.1, ape.parameters(2)*0.1)
      }
    }
    // Approximator for things that rise at once, then fall gradually
    object RiseFall extends ApproximatorCompanion[Biexponential] {
      def guess(ts: Array[Double], xs: Array[Double], finitize: Boolean): List[Approximator.Biexponential] = {
        if (ts.length < 1 || xs.length != ts.length) return Nil
        var xmax = xs(0)
        var xmin = xs(0)
        var imax, imin = 0
        var i = 1
        while (i < xs.length) {
          if (xs(i).finite) {
            if (xs(i) > xmax) {
              imax = i
              xmax = xs(i)
            }
            else if (xs(i) < xmin) {
              imin = i
              xmin = xs(i)
            }
          }
          i += 1
        }
        val x0 = xs(0)
        val x1 = xs(xs.length - 1)
        val xA = if (imax == 0) xmin else xmax
        val t0 = ts(0)
        val tA = ts(if (imax == 0) imin else imax)
        val t1 = ts(ts.length - 1)
        val tc1 = if (tA == t0) 0f else (1/(tA - t0)).toFloat
        new Approximator.Biexponential(x1, x0-xA, -tc1*(x0-xA), xA-x1, -(tc1/2)*(xA-x1)) :: Nil
      }
    }
  }

  final class Power(offset: Double, amplitude: Double, slope: Double) extends Approximator with Copy[Power] {
    def name = "Power"
    override def normalize = { if (parameters(1) closeTo 0) { parameters(1) = 0; parameters(2) = 0 }; this }
    def prettyArgs(inName: String, figs: Int) = {
      Constant.prettyValue(parameters(0), figs) +
      (if (parameters(1) < 0) " - " + Constant.prettyValue(-parameters(1), figs) else " + " + Constant.prettyValue(parameters(1), figs)) +
      "*" + inName + "^" +
      (
        if (parameters(1) == 0) "0"
        else {
          val alpha = parameters(2)/parameters(1)
          if (alpha < 0) "(" + Constant.prettyValue(alpha, figs) + ")"
          else Constant.prettyValue(alpha, figs)
        }
      )
    }
    val parameters = Array(offset, amplitude, slope)
    def copy = new Power(parameters(0), parameters(1), parameters(2))
    def exponent =  if (parameters(1) == 0) 0 else parameters(2)/parameters(1)
    def apply(t: Double) = 
      if (t <= 0) { 
        if (slope >= 0) parameters(0)
        else if (amplitude >= 0) Double.PositiveInfinity
        else Double.NegativeInfinity
      }
      else {
        val h = parameters(1)
        parameters(0) + (if (h != 0) h*pow(t, parameters(2)/h) else 0.0)
      }
    override def toString = f"x = ${parameters(0)} + ${parameters(1)}*t^$exponent"
  }
  object Power extends ApproximatorCompanion[Power] {
    def guess(ts: Array[Double], xs: Array[Double], finitize: Boolean = true): List[Power] = {
      val (left, right, fts, fxs, winner) = findCleanLeftRightSlopes(ts, xs, finitize)
      if ((left eq null) || (right eq null) || (fts eq null) || (fxs eq null)) return Nil
      if (fts(0) < 0) return Nil // Powers of negative values are a mess.
      val leftly =
        if (winner >= 6) Nil
        else {
          val k = 1 + (if (left.betaX * right.betaX > 0) max(-5, log(right.betaX/left.betaX)) else -5)/log(right.meanT/left.meanT)
          val ltPk = pow(left.meanT, k)
          val a = (left.meanX - right.meanX)/(ltPk - pow(right.meanT, k))
          val x0 = left.meanX - a*ltPk
          new Power(x0, a, k*a) :: Nil
        }
      val rightly =
        if (winner <= -6) Nil
        else {
          val k = 1 + (if (left.betaX * right.betaX > 0) min(5, log(right.betaX/left.betaX)) else 5)/log(right.meanT/left.meanT)
          val rtPk = pow(right.meanT, k)
          val a = (left.meanX - right.meanX)/(pow(left.meanT, k) - rtPk)
          val x0 = right.meanX - a*rtPk
          new Power(x0, a, k*a) :: Nil
        }
      val affly =
        if (winner.abs >= 6) Nil
        else {
          Affine.guess(fts, fxs, false).map{ aff =>
            new Power(aff.parameters(0), aff.parameters(1), aff.parameters(1))
          }
        }
      val curvely = 
        if (leftly.size == rightly.size) {
          if ((leftly zip rightly).forall{ case (l, r) =>
            signum(l.parameters(1)) == signum(r.parameters(1)) &&
            (l.exponent - r.exponent).abs/(l.exponent.abs + r.exponent.abs) < 0.05
          }) leftly
          else leftly ::: rightly
        }
        else leftly ::: rightly
      if (curvely.size == affly.size) {
        if ((curvely zip affly).forall{ case (l, r) => 
          signum(l.parameters(1)) == signum(r.parameters(1)) &&
          (l.exponent - 1).abs/(l.exponent.abs + 1) < 0.05/min(100, (fts(fts.length-1)/fts(0)))
        }) affly
        else curvely ::: affly
      }
      else curvely ::: affly
    }
  }

  final class Affexp(offset: Double, slope: Double, amplitude: Double, rate: Double) extends Approximator with Copy[Affexp] {
    def name = "Affexp"
    def prettyArgs(inName: String, figs: Int) =
      Constant.prettyValue(parameters(0), figs) + " + " +
      Constant.prettyValue(parameters(1), figs) + "*" + inName + " + " +
      Constant.prettyValue(parameters(2), figs) +
      " * e^(" + Constant.prettyValue(parameters(3), figs) + "*" + inName + ")"
    val parameters = Array(offset, slope, amplitude, rate)
    def copy = new Affexp(parameters(0), parameters(1), parameters(2), parameters(3))
    def apply(t: Double) = parameters(0) + parameters(1)*t + parameters(2)*math.exp(parameters(3)*t)
    override def toString = f"x = ${parameters(0)} + ${parameters(1)}*t + ${parameters(2)}*e^(${parameters(3)}*t)"
  }
  object Affexp extends ApproximatorCompanion[Affexp] {
    def guess(ts: Array[Double], xs: Array[Double], finitize: Boolean = true): List[Affexp] = Exponential.guess(ts, xs, finitize).map{ e =>
      new Affexp(e.parameters(0), 0, e.parameters(1), if (e.parameters(1) closeTo 0) -1e-10 else e.parameters(2)/e.parameters(1))
    }
  }

  final class Powexp(offset: Double, amplitude: Double, rate: Double, alpha: Double) extends Approximator with Copy[Powexp] {
    def name = "Powexp"
    def prettyArgs(inName: String, figs: Int) =
      Constant.prettyValue(parameters(0), figs) + " + " + Constant.prettyValue(parameters(1), figs) +
      (
        if (parameters(3) == 1) " * e^(" + Constant.prettyValue(parameters(2), figs) + "*" + inName + ")"
        else
          " * e^[" + Constant.prettyValue(parameters(2), figs) + " * |" + inName + "|^" + 
          (
            if (parameters(3) < 0) "(" + Constant.prettyValue(parameters(3), figs) + ")"
            else Constant.prettyValue(parameters(3), figs)
          ) + "]"
      )
    val parameters = Array(offset, amplitude, rate, alpha)
    def copy = new Powexp(parameters(0), parameters(1), parameters(2), parameters(3))
    def apply(t: Double) = 
      parameters(0) + parameters(1) * math.exp(parameters(2) * (if (parameters(3)==1) t else math.pow(math.abs(t), parameters(3))))
    override def toString = f"x = ${parameters(0)} + ${parameters(1)}*e^[${parameters(2)}*t^(${parameters(3)})]"
  }
  object Powexp extends ApproximatorCompanion[Powexp] {
    def guess(ts: Array[Double], xs: Array[Double], finitize: Boolean = true): List[Powexp] = Exponential.guess(ts, xs, finitize).map{ e =>
      new Powexp(e.parameters(0), e.parameters(1), if (e.parameters(1) closeTo 0) -1e-10 else e.parameters(2)/e.parameters(1), 1.0)
    }
  }

  final class Bilinear(t0: Double, x0: Double, leftSlope: Double, rightSlope: Double) extends Approximator with Copy[Bilinear] {
    def name = "Bilinear"
    def prettyArgs(inName: String, figs: Int) =
      Constant.prettyValue(parameters(1), figs) + " + (" + inName +
      (if (parameters(0) < 0) " + " + Constant.prettyValue(-parameters(0), figs) else " - " + Constant.prettyValue(parameters(0), figs)) +
      ")*{<0: " + Constant.prettyValue(parameters(2), figs) +
      "; >0: " + Constant.prettyValue(parameters(3), figs) +
      "}"
    val parameters = Array(t0, x0, leftSlope, rightSlope)
    def copy = new Bilinear(parameters(0), parameters(1), parameters(2), parameters(3))
    def apply(t: Double) = { val dt = t - parameters(0); parameters(1) + dt*(if (dt < 0) parameters(2) else parameters(3)) }
    override def toString = f"x = ${parameters(1)} + (t - ${parameters(0)})*{ t < ${parameters(0)}: ${parameters(2)}; otherwise ${parameters(3)} }"
  }
  object Bilinear extends ApproximatorCompanion[Bilinear] {
    def guess(ts: Array[Double], xs: Array[Double], finitize: Boolean = true): List[Bilinear] = Affine.guess(ts, xs, finitize).map{ aff =>
      var i = ts.length/2
      while (i < ts.length && !ts(i).finite) i += 1
      if (i >= ts.length) {
        i = ts.length/2 - 1
        while (i >= 0 && !ts(i).finite) i -= 1
        if (i < 0) i = 0
      }
      new Bilinear(ts(i), aff(ts(i)), aff.parameters(1), aff.parameters(1))
    }
  }
}

case class Optimized[+A <: Approximator](app: A, error: Double, evaluations: Long) {}

abstract class Optimizer {
  def name: String
  def verifiedFinite: Boolean
  def ts: Array[Double]
  def xs: Array[Double]
  def ws: Array[Double]  // null means all weights are 1!
  var smallEnoughError: Double = 1e-4
  var worthwhileImprovement: Double = 1e-5
  var iterationBudget: Long = 100000
  def setTargets(absolute: Double, improve: Double, iterations: Long): this.type = { 
    if (!absolute.nan) smallEnoughError = absolute
    if (!improve.nan) worthwhileImprovement = improve.abs
    if (iterations == 0) iterationBudget = Long.MaxValue else if (iterations > 0) iterationBudget = iterations
    this
  }
  def apply[A <: Approximator](initial: Array[A]): List[Optimized[A]]
  def from[A <: Approximator : ClassTag](guessers: ApproximatorCompanion[A]*): List[Optimized[A]] =
    apply(guessers.flatMap(_.guess(ts, xs, !verifiedFinite)).toArray)
}

trait OptimizerCompanion[+Opt <: Optimizer] {
  def over(ts: Array[Double], xs: Array[Double], ws: Array[Double]): Opt
  def over(ts: Array[Double], xs: Array[Double]): Opt = over(ts, xs, null)

  def candidates[A <: Approximator : ClassTag](
    absoluteE: Double, improveE: Double, iterN: Long,
    ts: Array[Double], xs: Array[Double], ws: Array[Double],
    guessers: ApproximatorCompanion[A]*
  ): List[Optimized[A]] =  over(ts, xs, ws).setTargets(absoluteE, improveE, iterN).from(guessers: _*)

  def candidates[A <: Approximator : ClassTag](
    absoluteE: Double, improveE: Double, iterN: Long, ts: Array[Double], xs: Array[Double], guessers: ApproximatorCompanion[A]*
  ): List[Optimized[A]] =  over(ts, xs, null).setTargets(absoluteE, improveE, iterN).from(guessers: _*)

  def candidates[A <: Approximator : ClassTag](
    ts: Array[Double], xs: Array[Double], ws: Array[Double], guessers: ApproximatorCompanion[A]*
  ): List[Optimized[A]] = over(ts, xs, ws).from(guessers: _*)

  def candidates[A <: Approximator : ClassTag](
    ts: Array[Double], xs: Array[Double], guessers: ApproximatorCompanion[A]*
  ): List[Optimized[A]] = over(ts, xs, null).from(guessers: _*)

  private def bestOf[A <: Approximator](os: List[Optimized[A]]) = os.reduceOption((l, r) => if (l.error <= r.error) l else r)

  def apply[A <: Approximator : ClassTag](
    absoluteE: Double, improveE: Double, iterN: Long,
    ts: Array[Double], xs: Array[Double], ws: Array[Double],
    guessers: ApproximatorCompanion[A]*
  ): Option[Optimized[A]] =  bestOf(candidates(absoluteE, improveE, iterN, ts, xs, ws, guessers: _*))

  def apply[A <: Approximator : ClassTag](
    absoluteE: Double, improveE: Double, iterN: Long, ts: Array[Double], xs: Array[Double], guessers: ApproximatorCompanion[A]*
  ): Option[Optimized[A]] = bestOf(candidates(absoluteE, improveE, iterN, ts, xs, guessers: _*))

  def apply[A <: Approximator : ClassTag](
    ts: Array[Double], xs: Array[Double], ws: Array[Double], guessers: ApproximatorCompanion[A]*
  ): Option[Optimized[A]] = bestOf(candidates(ts, xs, ws, guessers: _*))

  def apply[A <: Approximator : ClassTag](
    ts: Array[Double], xs: Array[Double], guessers: ApproximatorCompanion[A]*
  ): Option[Optimized[A]] = bestOf(candidates(ts, xs, guessers: _*))
}

abstract class VerifiedOptimizer(dataTs: Array[Double], dataXs: Array[Double], dataWs: Array[Double]) extends Optimizer {
  def verifiedFinite = true
  val (ts, xs, ws) =
    if ((dataWs eq null) || dataWs.length == 0) { val (nts, nxs) = DataShepherd.ensureFinite(dataTs, dataXs); (nts, nxs, dataWs) }
    else DataShepherd.ensureFinite(dataTs, dataXs, dataWs)
}

object Optimizer {
  def simpleConvex(t0: Double, t1: Double, tol: Double = 1e-8, expandLeft: Boolean = false, expandRight: Boolean = false)(f: Double => Double) = {
    var tA = t0
    var tD = t1
    var tB = (2*t0 + t1)/3
    var tC = (t0 + 2*t1)/3
    var xA = f(tA)
    var xB = f(tB)
    var xC = f(tC)
    var xD = f(tD)
    if (expandLeft || expandRight) {
      while (xA < xB && xA < xC && xA < xD && expandLeft && tA > -1e300) {
        tA += tA - tD
        tB = (2*tA + tD)/3
        tC = (tA + 2*tD)/3
        xA = f(tA)
        xB = f(tB)
        xC = f(tC)
      }
      while (xD < xC && xD < xB && xD < xA && expandRight && tD < 1e300) {
        tD += tD - tA
        tB = (2*tA + tD)/3
        tC = (tA + 2*tD)/3
        xD = f(tD)
        xB = f(tB)
        xC = f(tC)
      }
    }
    while ((tD - tA) > tol && 2*(tD - tA) > tol*(tD.abs + tA.abs)) {
      val m = math.min(xB, xC)
      if (m <= xA && m <= xD) {
        if (xB < xC) {
          xD = xC
          tD = tC
        }
        else if (xC < xB) {
          xA = xB
          tA = tB
        }
        else {
          xA = xB
          xD = xC
          tA = tB
          tD = tC
        }
      }
      else if (xA < xD) {
        xD = xB
        tD = tB
      }
      else {
        xA = xC
        tA = tC
      }
      tB = (2*tA + tD)/3
      tC = (tA + 2*tD)/3
      xB = f(tB)
      xC = f(tC)
    }
    (tA + tD)/2
  }

  final class Hyphae(dataTs: Array[Double], dataXs: Array[Double], dataWs: Array[Double]) extends VerifiedOptimizer(dataTs, dataXs, dataWs) {
    def name = "Hyphae"
    private case class Tip[A <: Approximator](
      app: A,
      scales: Array[Double],
      scores: Array[Double],
      budget: Array[Float],
      ranking: Array[Int],
      var evals: Long = 0
    ) {
      private[this] def mseUnweighted: Double = {
        var sum = 0.0
        var i = 0
        while (i < ts.length) {
          sum += (app(ts(i)) - xs(i)).sq
          i += 1
        }
        evals += 1
        sum/ts.length
      }
      private[this] def mseWeighted: Double = {
        var sum = 0.0
        var W = 0.0
        var i = 0
        while (i < ts.length) {
          W += ws(i)
          sum += ws(i)*(app(ts(i)) - xs(i)).sq
          i += 1
        }
        evals += 1
        if (W != 0) sum/W else sum      
      }
      def mse = if (ws eq null) mseUnweighted else mseWeighted
      def mseChanging(index: Int, amount: Double): Double = {
        val temp = app.parameters(index)
        app.parameters(index) += amount
        val ans = mse
        app.parameters(index) = temp
        //println(f"$index ${app.parameters(index)+amount} ${ans - bestError} $bestError")
        ans
      }
      private[this] var bestError = mse
      def lastError = bestError
      private[this] def improveOnce(): Int = {
        var i = 0
        var improvements = 0
        while (i < ranking.length) {
          val j = ranking(i)-1
          if (budget(j) >= 1) {
            budget(j) -= 1
            if (scales(j).abs < math.max(1e-12, app.parameters(j)*1e-8)) {
              val initial = math.max(1e-6, 1e-6*app.parameters(j).abs)
              scales(j) = initial
              var e = mseChanging(j, scales(j))
              while ((e closeTo bestError) && scales(j) < initial*2e6) { scales(j) *= 10; e = mseChanging(j, scales(j)) }
              if (e > bestError) { scales(j) = -scales(j); e = mseChanging(j, scales(j)) }
              var improving = e < bestError
              val ever = improving
              while (improving) {
                val nu = mseChanging(j, 10*scales(j))
                if (nu < e) {
                  scales(j) = 10*scales(j)
                  e = nu
                }
                else improving = false
              }
              if (ever) {
                bestError = e
                app.parameters(j) += scales(j)
              }
            }
            val eFwd = mseChanging(j, scales(j))
            var better = bestError
            if (eFwd < bestError) {
              better = eFwd
              val eFwd2 = mseChanging(j, 2*scales(j))
              if (eFwd2 < eFwd) {
                better = eFwd2
                scales(j) *= 2 
                app.parameters(j) += scales(j)
              }
              else {
                app.parameters(j) += scales(j)
                scales(j) *= NumericConstants.TwoOverPi     // Reduce step size by a non-integer factor so we don't fall into loops
              }
            }
            else {
              val eBkw = mseChanging(j, -scales(j))
              if (eBkw < bestError) {
                better = eBkw
                app.parameters(j) -= scales(j)
                scales(j) = -scales(j)
              }
              else {
                if (eFwd == bestError && eBkw == bestError) scales(j) *= 10  // Arguably just taking too small of steps!
                else scales(j) *= -NumericConstants.OverE                    // Back up slowly
              }
            }
            scores(j) = (scores(j) + max(0, bestError - better))/2
            if (better < bestError) {
              bestError = better
              improvements += 1
              if (i > 0 && scores(j) > scores(ranking(i-1)-1)) {
                // Reward good performance by bumping up the ranking
                val jj = ranking(i-1)-1
                ranking(i-1) = j+1
                ranking(i) = jj+1
              }
            }
          }
          i += 1
        }
        i = 0
        while (i < ranking.length) {
          budget(ranking(i)-1) += (1.0/(i+1.1217010001)).toFloat  // Approximate power law distribution of effort into parameters--weird constant avoids same parameters coming up next to each other over and over again 
          i += 1
        }
        improvements
      }
      // Try to do gradient thing.  Doesn't seem to work very well.  Bugs, maybe?  Or just standard "gradients are myopic"?
      private[this] def improveAllFrom(old: Array[Double], scratch: Array[Double] = null): Int = {
        var i = 0
        val grad = 
          if ((scratch eq null) || scratch.length < app.parameters.length) java.util.Arrays.copyOf(app.parameters, app.parameters.length)
          else {
            System.arraycopy(app.parameters, 0, scratch, 0, app.parameters.length)
            scratch
          }

        i = 0
        while (i < app.parameters.length) { 
          grad(i) = (grad(i) - old(i))/10
          old(i) = app.parameters(i)
          app.parameters(i) += grad(i)
          i += 1
        }
        var multiplier = 1.0
        var e = mse
        //val be = bestError

        if (e > bestError) {
          multiplier = -1.0
          i = 0; while (i < app.parameters.length) { app.parameters(i) = old(i) - grad(i); i += 1 }
          val e = mse
        }

        if (e < bestError) {
          var lastmult = 0.0
          var nextmult = multiplier
          multiplier = 0
          var laste = bestError
          var nexte = e
          e = laste
          while (nexte >= e) {
            lastmult = multiplier
            laste = e
            multiplier = nextmult
            e = nexte
            nextmult = multiplier * 2
            i = 0; while (i < app.parameters.length) { app.parameters(i) = old(i) + nextmult*grad(i); i += 1 }
            nexte = mse
          }
          // Quadratic fit to best multiplier
          if (lastmult != multiplier && lastmult != nextmult) {
            // Assuming form a0*m^2 + a1*m + a2 = 0
            val qmult = 
              -(nextmult.sq*(e - laste) - multiplier.sq*(nexte - laste) + lastmult.sq*(nexte - e)) /
              (2*(nextmult*(e - laste) - multiplier*(nexte - laste) + lastmult*(nexte - e)))
            i = 0; while (i < app.parameters.length) { app.parameters(i) = old(i) + nextmult*grad(i); i += 1 }
            val qerr = mse
            if (qerr < e) bestError = qerr
            else {
              i = 0;while (i < app.parameters.length) { app.parameters(i) = old(i) + multiplier*grad(i); i += 1 }
              bestError = e
            }
          }
          //println(f"$be -> $bestError")
          //println(f"  ${old.take(app.parameters.length).mkString(",")}")
          //println(f"  ${app.parameters.mkString(",")}")
          app.parameters.length
        }
        else {
          System.arraycopy(old, 0, app.parameters, 0, app.parameters.length)
          //println(f"$e XX $bestError")
          //println(f"  ${old.take(app.parameters.length).mkString(",")}")
          //println(f"  ${app.parameters.mkString(",")}")
          0
        }
      }
      private[optimization] def improveEpoch(scratchOne: Array[Double] = null, scratchTwo: Array[Double] = null): (Int, Double, Double) = {
        var n = 0
        var imp = 0
        val before = bestError
        val scratch =
          if ((scratchOne eq null) || scratchOne.length < app.parameters.length) java.util.Arrays.copyOf(app.parameters, app.parameters.length)
          else {
            System.arraycopy(app.parameters, 0, scratchOne, 0, app.parameters.length)
            scratchOne
          }
        while (n < app.parameters.length) {
          imp += improveOnce()
          n += 1
        }
        val after = bestError // if (bestError < before) { imp += improveAllFrom(scratch, scratchTwo); bestError } else bestError
        val better = before - after
        val fracbetter = better.abs / max(before.abs, after.abs)
        (imp, better, fracbetter)
      }
    }
    def apply[A <: Approximator](initial: Array[A]): List[Optimized[A]] = {
      val tips = initial.map{ app => 
        val np = app.parameters.length
        Tip(app, Array.fill(np)(0.0), Array.fill(np)(0.0), Array.fill(np)(2f), Array.range(1, np+1), 0)
      }
      val aux = new Array[(Int, Double, Double)](tips.length)
      var n = tips.length
      var promising = true
      var epoch = 0
      var evals = 0L
      while (promising) {
        epoch += 1
        evals = 0L
        var i = 0; while (i < n) { aux(i) = tips(i).improveEpoch(); evals += tips(i).evals; i += 1 }
        if (n > 1 && epoch > 10) {
          var worst = 0
          i = 1
          while (i < n) {
            if (tips(worst).lastError < tips(i).lastError) worst = i
            i += 1
          }
          var excused = false
          i = 0
          while (i < n && !excused) {
            excused = (i != worst) && (aux(i)._2 < aux(worst)._2 || aux(i)._3 < aux(worst)._3)
            i += 1
          }
          if (!excused) {
            val temp = tips(worst)
            tips(worst) = tips(n-1)
            tips(n-1) = temp
            n -= 1
          }
        }
        var anybetter = false
        i = 0
        while (i < aux.length && !anybetter) {
          anybetter = aux(i)._1 > 0 && aux(i)._3 >= worthwhileImprovement && !(tips(i).lastError < smallEnoughError)
          i += 1
        }
        promising = (epoch < 5 || anybetter) && !(evals > iterationBudget)
      }
      tips.take(n).sortBy(_.lastError).map(tip => Optimized(tip.app.normalize, tip.mse, tip.evals)).toList
    }
  }
  object Hyphae extends OptimizerCompanion[Hyphae] {
    def over(ts: Array[Double], xs: Array[Double], ws: Array[Double]) = new Hyphae(ts, xs, ws)
  }

  // Based directly on http://www.caam.rice.edu/tech_reports/1990/TR90-07.pdf, V. Torczon's Ph.D. thesis (1989).
  final class Torczon(dataTs: Array[Double], dataXs: Array[Double], dataWs: Array[Double]) extends VerifiedOptimizer(dataTs, dataXs, dataWs) {
    def name = "Torczon"
    val ys = new Array[Double](xs.length)
    private var evaluations = 0L
    private class Plex(
      corners: Array[Array[Double]], edges: Array[Array[Double]], scratch: Array[Array[Double]],
      values: Array[Double], trials: Array[Double], retrials: Array[Double], oldroot: Array[Double]
    ) {
      var best: Int = 0
      private def meanSqError: Double = {
        var i = 0
        var esq = 0.0
        while (i < xs.length) {
          val err = xs(i) - ys(i)
          esq += err*err
          i += 1
        }
        if (i == 0) 0 else esq/i
      }
      private def meanSqErrorWeighted: Double = {
        var i = 0
        var esq = 0.0
        var w = 0.0
        while (i < xs.length) {
          w += ws(i)
          val err = xs(i) - ys(i)
          esq += err*err*ws(i)
          i += 1
        }
        if (w == 0) 0 else esq/w
      }
      private def scoreWith(app: Approximator): Double = {
        app.computeInto(ts, ys)
        evaluations += 1
        if (ws ne null) meanSqErrorWeighted else meanSqError
      }
      private def scoresAtVertices(app: Approximator, verts: Array[Array[Double]], scores: Array[Double]): Int = {
        var i = 0
        var bmse = Double.PositiveInfinity
        var bidx = -1 
        while (i < verts.length) {
          val vi = verts(i)
          var j = 0
          while (j < vi.length) {
            app.parameters(j) = vi(j)
            j += 1
          }
          val mse = scoreWith(app)
          scores(i) = mse
          if (mse < bmse) { bidx = i; bmse = mse }
          i += 1
        }
        bidx
      }
      private def walkEdges(home: Array[Double], scale: Double) {
        var i = 0
        while (i < corners.length) {
          val ei = edges(i)
          val si = scratch(i)
          var j = 0
          while (j < home.length) {
            si(j) = home(j) + scale*ei(j)
            j += 1
          }
          i += 1
        }
      }
      private def acceptScratch() {
        var k = 0; 
        while (k < corners.length) {
          val temp = corners(k)
          corners(k) = scratch(k)
          scratch(k) = temp
          k += 1
        }
      }
      private def reEdge(index: Int, scores: Array[Double]) {
        val home = corners(index)
        var i = 0
        while (i < corners.length) {
          val ei = edges(i)
          val ci = corners(i)
          var j = 0
          while (j < home.length) {
            ei(j) = ci(j) - home(j)
            j += 1
          }
          i += 1
        }
        best = index
        if (scores ne values) System.arraycopy(scores, 0, values, 0, scores.length)
      }
      def evaluateWith(app: Approximator): Int = { scoresAtVertices(app, corners, values) }
      def testWith(app: Approximator, home: Array[Double], scale: Double, scores: Array[Double]): Int = {
        walkEdges(home, scale)
        scoresAtVertices(app, scratch, scores)
      }
      def iterateOn(app: Approximator): Double = {
        val eb = values(best)
        val i = testWith(app, corners(best), -1.0, trials)
        if (trials(i) < eb) {
          // New best result!
          val ei = trials(i)
          System.arraycopy(corners(best), 0, oldroot, 0, oldroot.length)
          acceptScratch()
          val i2 = testWith(app, oldroot, -2.0, retrials)
          if (retrials(i2) < ei) {
            acceptScratch()
            reEdge(i2, retrials)
          }
          else reEdge(i, trials)
        }
        else {
          val ih = testWith(app, corners(best), 0.5, trials)
          if (trials(ih) < eb) {
            acceptScratch()
            reEdge(ih, trials)
          }
          else {
            var k = 0
            while (k < edges.length) {
              val ek = edges(k)
              var l = 0
              while (l < ek.length) {
                ek(l) *= 0.5
                l += 1
              }
              values(k) = trials(k)
              k += 1
            }
          }
        }
        eb - values(best)
      }
      def initializeAt(app: Approximator, minDiff: Double = 1e-5) {
        System.arraycopy(app.parameters, 0, corners(0), 0, app.parameters.length)
        var i = 0
        val e = scoreWith(app)
        while (i < app.parameters.length) {
          val pi = app.parameters(i)
          var diff = 0.0
          var delta = math.max((app.parameters(i)*minDiff).abs, minDiff.abs)/10
          while (diff < minDiff && delta.finite) {
            delta *= 10
            app.parameters(i) = pi + delta
            val ep = scoreWith(app)
            app.parameters(i) = pi - delta
            val en = scoreWith(app)
            diff = math.max((ep-e).abs, (en-e).abs)
          }
          while (diff > 10*minDiff) {
            delta *= 0.6
            app.parameters(i) = pi + delta
            val ep = scoreWith(app)
            app.parameters(i) = pi - delta
            val en = scoreWith(app)
            diff = math.max((ep-e).abs, (en-e).abs)
          }
          System.arraycopy(app.parameters, 0, corners(i+1), 0, app.parameters.length)
          corners(i+1)(i) += (if (delta.finite) delta else 1 + app.parameters(i)*2)
          i += 1
        }
        best = evaluateWith(app)
        reEdge(best, values)
      }
      def bestInto(app: Approximator): app.type = {
        System.arraycopy(corners(best), 0, app.parameters, 0, app.parameters.length)
        app
      }
      def bestError: Double = values(best)
      def evals: Long = evaluations
    }
    private object Plex {
      def ofDim(n: Int) = new Plex(
        Array.fill(n + 1, n)(0.0), Array.fill(n + 1, n)(0.0), Array.fill(n+1, n)(0.0),
        Array.fill(n + 1)(0.0), Array.fill(n + 1)(0.0), Array.fill(n + 1)(0.0), Array.fill(n)(0.0)
      )
    }
    def apply[A <: Approximator](app: A): Optimized[A] = {
      val plex = Plex.ofDim(app.parameters.length)
      plex initializeAt app
      var minimalProgress = -10
      var err = Double.PositiveInfinity
      while (minimalProgress < 10 && err > smallEnoughError && plex.evals < iterationBudget) {
        val improvement = plex iterateOn app
        if (improvement == 0 || improvement < max(worthwhileImprovement.sq, plex.bestError*worthwhileImprovement)) minimalProgress += 1
        else minimalProgress = math.min(minimalProgress, 0)
      }
      plex bestInto app
      Optimized(app.normalize, plex.bestError, plex.evals)
    }
    def apply[A <: Approximator](initial: Array[A]): List[Optimized[A]] = initial.map(app => this apply app).toList
 }
  object Torczon extends OptimizerCompanion[Torczon] {
    def over(ts: Array[Double], xs: Array[Double], ws: Array[Double]) = new Torczon(ts, xs, ws)
  }
}
