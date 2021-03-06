package kse.tests

import language.implicitConversions

import kse.typecheck._
import kse.flow._

trait Test_Kse { self =>
  protected implicit def enablePrintedEquals[A](a: A) = new Test_Kse.EnablePrintedEquals(a)
  
  protected def explainThrowable(t: Throwable)(implicit extraTitle: ImplicitValue[String, Test_Kse]): Vector[String] = {
    val title = t.getClass.getName + Option(t.getMessage).map(": " + _).getOrElse("")
    if (extraTitle.value == null || extraTitle.value == "") title +: t.getStackTrace.map("  " + _).toVector
    else (Array(extraTitle.value, title) ++ t.getStackTrace.map("  " + _)).toVector
  }

  private val registration = Test_Kse.register(this)

  def run: Ok[Vector[Vector[String]],Int] = {
    val ms = this.getClass.getMethods.
      filter(_.getName.startsWith("test_")).
      filter{ m =>
        import java.lang.reflect.Modifier._
        (m.getModifiers & (PUBLIC | ABSTRACT)) == PUBLIC
      }.
      filter{ m =>
        val r = m.getReturnType
        (classOf[Boolean] isAssignableFrom r) || (classOf[Test_Kse.TestResult] isAssignableFrom r)
      }.
      groupBy(_.getName).
      mapValues(_.reduce{ (m,n) =>
        if (m.getReturnType isAssignableFrom n.getReturnType) m
        else if (n.getReturnType isAssignableFrom m.getReturnType) n
        else return No(Vector(Vector("Incompatible return types in methods named " + m.getName)))
      }).
      map(_._2).toArray.
      sortBy(_.getName)
    val oks = ms.map{ m =>
      implicit val title = new ImplicitValue[String, Test_Kse] { def value = "In " + m.getName }
      safeHop(explainThrowable){ fail =>
        val o: Any = try { m.invoke(self) } catch { case ite: java.lang.reflect.InvocationTargetException => if (ite.getCause != null) throw ite.getCause else throw ite }
        o match {
          case b: Boolean if b == false => fail(Vector(m.getName + " returned false."))
          case Test_Kse.TestResult(Some(v)) => fail(v)
          case _ => ()
        }
      }
    }
    No( oks.collect{ case No(n) => n }.toVector ) accept {
      case v if v.length == 0 => oks.length
    }
  }

  protected def tidyClassName(c: Class[_]): String = {
    var n = c.getName.split('.').last
    if (n.endsWith("$")) n = n.substring(0, n.length-1)
    if (n.startsWith("Test_")) n = n.substring(5)
    n
  }

  def typicalMain(args: Array[String]) {
    run match {
      case Yes(n) => println(s"No failures in ${tidyClassName(getClass)}, $n tests passed!")
      case No(v) => 
        v foreach { u =>
          u.foreach(println)
          println
        }
        println(s"${v.length} failures in ${tidyClassName(getClass)}.")
        sys.exit(1)
    }
  }
}

object Test_Kse {
  private val myRegistered = new collection.mutable.ArrayBuffer[Test_Kse]()

  def registered: Seq[Test_Kse] = myRegistered
  def register(tk: Test_Kse) = { myRegistered += tk; registered.length }

  case class TestResult(result: Option[Vector[String]] = None) {}

  class EnablePrintedEquals[A](private val underlying: A) extends AnyVal {
    def =?=[B](overlying: B) = {
      val ans = underlying == overlying
      if (!ans) { println("Not equal:"); println("  "+underlying); println("  "+overlying) }
      ans
    }
    def expectHere[B](msg: String)(overlying: B) = {
      val ans = underlying == overlying
      if (!ans) { println(s"Not equal at $msg"); println("  " + underlying); println("  " + overlying) }
      ans
    }
  }  
}
