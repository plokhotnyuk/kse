package kse.tests

import kse.flow._
import kse.coll.packed._
import kse.eio._

object Test_Grok extends Test_Kse {
  import kse.eio.{GrokErrorCodes => e}
  
  val mkGroks = Array[String => Grok](s => Grok(s), s => Grok text s.getBytes)
  
  case class Secret[A](known: A)(unknown: => String) {
    override def equals(o: Any) = known == o
    override def toString = known.toString + "/*" + unknown + "*/"
  }
  
  implicit class GrokResultIsNo[A](no: Ok[GrokError, A]) {
    def isNo(g: Grok, who: Set[Int], why: Set[Int]) = no match {
      case No(ge: GrokError) if who(ge.whoError) && why(ge.whyError) && g.errorCode == ge.whyError => true
      case No(ge: GrokError) => println(ge); println(ge.whoError + " " + who); println(ge.whyError + " " + why + " " + g.errorCode); false
      case Yes(a) => println(a); println(who); println(why); false
    }
    def isNo(g: Grok, who: Int, why: Int): Boolean = isNo(g, Set(who), Set(why))
  }
  
  val validSkipOne = Array("", "blah", "la la", " ")
  val validSkipThree = Array("la la la", "salmon, cod, pike, and bass", "     ")
  def test_skip = mkGroks.forall{ mkGrok =>
    (validSkipOne ++ validSkipThree).forall{ s => val g = mkGrok(s); g{ implicit fail => g.skip }.isOk && (s == "" || g.position > 0) } &&
    validSkipThree.forall{ s => (1 to 3).forall{ n => val g = mkGrok(s); g{ implicit fail => g.skip(n) }.isOk && g.position > 0 } } &&
    validSkipOne.forall{s => val g = mkGrok(s); g{ implicit fail => g.skip; g.skip; g.skip }.isNo(g , e.tok, e.end) } &&
    validSkipOne.forall{s => val g = mkGrok(s); g{ implicit fail => g.skip(3) }.isNo(g, e.tok, e.end) } &&
    validSkipThree.forall{ s => val g = mkGrok(s); g{ implicit fail => g.skip(100) }.isNo(g , e.tok, e.end) }
  }

  val validBool = Array("true", "false", "True", "False", "TRUE", "FALSE", "trUE", "fAlSe")
  val invalidBool = Array("treu", "t", "fals", "f")
  def test_Z = mkGroks.forall{ mkGrok =>
    validBool.forall{ s => val g = mkGrok(s); g{ implicit fail => g.Z } == Yes(s.toBoolean) } &&
    invalidBool.forall{ s => val g = mkGrok(s); g{ implicit fail => g.Z }.isNo(g , Set(e.Z), Set(e.wrong, e.end)) }
  }
  
  val trueAnyBool = Array("yes", "Y", "on", "T", "True", "TRUE", "yeS", "1")
  val falseAnyBool = Array("no", "Off", "n", "falSe", "0")
  val invalidAnyBool = Array("ye", "nay", "tr", "tru", "fa", "of", "uh-huh")
  def test_aZ = mkGroks.forall{ mkGrok =>
    trueAnyBool.forall{s => val g = mkGrok(s); g{ implicit fail => g.aZ } == Yes(true) } &&
    falseAnyBool.forall{s => val g = mkGrok(s); g{ implicit fail => g.aZ } == Yes(false) } && 
    validBool.forall{s => val g = mkGrok(s); g{ implicit fail => g.aZ } == Yes(s.toBoolean) } &&
    invalidAnyBool.forall{ s => val g = mkGrok(s).delimit(true); g{ implicit fail => g.aZ }.isNo(g, Set(e.aZ), Set(e.wrong, e.end, e.delim)) } &&
    invalidAnyBool.forall{ s => val g = mkGrok(s); val ans = g{implicit fail => g.aZ}; (ans.isOk && g.position == 1) || ans.isNo(g, Set(e.aZ), Set(e.wrong, e.end)) } &&
    {
      // Catch end-of-token problems
      val g = mkGrok((trueAnyBool ++ falseAnyBool ++ trueAnyBool).mkString(" ")).delimit(true)
      g{ implicit fail => g.each{ g.aZ } }.exists(a => a.count(_ == true) =?= 2*trueAnyBool.length && a.count(_ == false) =?= falseAnyBool.length)
    }
  }
  
  val validSignedBytes = Array[Byte](1, 47, -125, 0, 127, -128)
  val invalidSignedBytes = Array[Int](-129, 128, 19238758, 1515, 1111999)
  val notevenNumbers = Array[String]("fish", "true", "  2")
  def test_B = mkGroks.forall{ mkGrok =>
    validSignedBytes.forall{ b => val g = mkGrok(b.toString); g{ implicit fail => g.B } == Yes(b) } &&
    invalidSignedBytes.forall{ b => val g = mkGrok(b.toString); g{ implicit fail => g.B }.isNo(g , e.B, e.range ) } &&
    notevenNumbers.forall{ b => val g = mkGrok(b); g{ implicit fail => g.B }.isNo(g, e.B, e.wrong ) }
  }
  
  val invalidUnsignedBytes = Array[Int](-1, 256, 1111999)
  def test_uB = mkGroks.forall{ mkGrok =>
    validSignedBytes.forall{ b => val g = mkGrok((b&0xFF).toString); g{ implicit fail => g.uB } == Yes(b) } &&
    invalidUnsignedBytes.forall{ b => val g = mkGrok(b.toString); g{ implicit fail => g.uB }.isNo(g, Set(e.uB), Set(e.range, e.wrong)) } &&
    notevenNumbers.forall{ b => val g = mkGrok(b); g{ implicit fail => g.uB }.isNo(g, e.uB, e.wrong) }
  }
  
  val validSignedShorts = Array[Short](1, 47, -125, 0, -32768, 32767, 11441)
  val invalidSignedShorts = Array[Int](-1923587, -32769, 32768, 987519, 11111199)
  def test_S = mkGroks.forall{ mkGrok =>
    validSignedShorts.forall{ s => val g = mkGrok(s.toString); g{ implicit fail => g.S } == Yes(s) } &&
    invalidSignedShorts.forall{ s => val g = mkGrok(s.toString); g{ implicit fail => g.S }.isNo(g, e.S, e.range) } &&
    notevenNumbers.forall{ s => val g = mkGrok(s); g{ implicit fail => g.S }.isNo(g, e.S, e.wrong) }
  }
  
  val invalidUnsignedShorts = Array[Int](-1, 65536, 11111199)
  def test_uS = mkGroks.forall{ mkGrok =>
    validSignedShorts.forall{ s => val g = mkGrok((s&0xFFFF).toString); g{ implicit fail => g.uS } == Yes(s) } &&
    invalidUnsignedShorts.forall{ s => val g = mkGrok(s.toString); g{ implicit fail => g.uS }.isNo(g, Set(e.uS), Set(e.range, e.wrong)) } &&
    notevenNumbers.forall{ s => val g = mkGrok(s); g{ implicit fail => g.uS }.isNo(g, e.uS, e.wrong) }
  }
  
  val validCharacters = Array("x", "!", ".", "\u1235")
  val invalidCharacters = Array("", " x")
  def test_C = mkGroks.forall{ mkGrok =>
    validCharacters.forall{ c => val g = Grok(c); g{ implicit fail => g.C.toString } == Yes(c) } &&
    invalidCharacters.forall{ c => val g = Grok(c); g{ implicit fail => g.C }.isNo(g, e.C, if (c.isEmpty) e.end else e.wrong ) }
  }
  
  val validSignedInts = Array[Int](0, 1, -92751, 2519875, Int.MaxValue, Int.MinValue)
  val invalidSignedInts = Array[Long](11111111111199L, Int.MaxValue + 1L, Int.MinValue -1L)
  def test_I = mkGroks.forall{ mkGrok =>
    validSignedInts.forall{ i => val g = mkGrok(i.toString); g{ implicit fail => g.I } == Yes(i) } &&
    invalidSignedInts.forall{ i => val g = mkGrok(i.toString); g{ implicit fail => g.I }.isNo(g, e.I, e.range) } &&
    notevenNumbers.forall{ i => val g = mkGrok(i); g{ implicit fail => g.I }.isNo(g, e.I, e.wrong) }
  }
  
  val invalidUnsignedInts = Array[Long](-1L, 1L << 32, 11111111111199L)
  def test_uI = mkGroks.forall{ mkGrok =>
    validSignedInts.forall{ i => val g = mkGrok((i & 0xFFFFFFFFL).toString); g{ implicit fail => g.uI } == Yes(i) } &&
    invalidUnsignedInts.forall{ i => val g = mkGrok(i.toString); g{ implicit fail => g.uI }.isNo(g, Set(e.uI), Set(e.range, e.wrong)) } &&
    notevenNumbers.forall{ i => val g = mkGrok(i); g{ implicit fail => g.uI }.isNo(g, e.uI, e.wrong) }
  }
  
  val invalidHexInts = Array[String]("100000000", "-1", "ffffffffff")
  def test_xI = mkGroks.forall{ mkGrok =>
    validSignedInts.forall{ i => val g = mkGrok((i & 0xFFFFFFFFL).toHexString); g{ implicit fail => g.xI } == Yes(i) } &&
    invalidHexInts.forall{ i => val g = mkGrok(i); g{ implicit fail => g.xI }.isNo(g, Set(e.xI), Set(e.range, e.wrong)) } &&
    notevenNumbers.forall{ i => val g = mkGrok(i); val ans = g{ implicit fail => g.xI }; (ans == Yes(0xF) && g.position == 1) || ans.isNo(g, e.xI, e.wrong) }
  }
  
  val invalidAnyInts = Array[String]((Int.MinValue-1L).toString, (1L << 32).toString, "0x100000000", "0xFFFFFFFFF")
  def test_aI = mkGroks.forall{ mkGrok =>
    validSignedInts.forall{ i => val g = mkGrok(i.toString); g{ implicit fail => g.aI } == Yes(i) } &&
    validSignedInts.forall{ i => val g = mkGrok((i & 0xFFFFFFFFL).toString); g{ implicit fail => g.aI } == Yes(i) } &&
    validSignedInts.forall{ i => val g = mkGrok("0x" + (i & 0xFFFFFFFFL).toHexString); g{ implicit fail => g.aI } == Yes(i) } &&
    validSignedInts.forall{ i => val g = mkGrok("0X" + (i & 0xFFFFFFFFL).toHexString); g{ implicit fail => g.aI } == Yes(i) } &&
    invalidAnyInts.forall{ i => val g = mkGrok(i); g{ implicit fail => g.aI }.isNo(g, Set(e.aI, e.I, e.xI, e.uI), Set(e.range)) } &&
    notevenNumbers.forall{ i => val g = mkGrok(i); g{ implicit fail => g.aI }.isNo(g, e.aI, e.wrong) }
  }
  
  val validSignedLongs = Array[Long](0, 1, -9815, 198751, -9175498191732L, 12341982791571L, Long.MaxValue, Long.MinValue)
  val invalidSignedLongs = Array[BigInt](BigInt("1111111111111111111111111111199"), BigInt(Long.MinValue)-1, BigInt(Long.MaxValue)+1)
  def test_L = mkGroks.forall{ mkGrok =>
    validSignedLongs.forall{ l => val g = mkGrok(l.toString); g{ implicit fail => g.L } == Yes(l) } &&
    invalidSignedLongs.forall{ l => val g = mkGrok(l.toString); g{ implicit fail => g.L }.isNo(g, e.L, e.range) } &&
    notevenNumbers.forall{ l => val g = mkGrok(l); g{ implicit fail => g.L }.isNo(g, e.L, e.wrong) }
  }
  
  val invalidUnsignedLongs = Array[BigInt](BigInt("-1"), BigInt("11111111111111111111111199"), BigInt(1) << 64)
  def test_uL = mkGroks.forall{ mkGrok =>
    validSignedLongs.forall{ l => val g = mkGrok((BigInt(l) & ((BigInt(1)<<64)-1)).toString); g{ implicit fail => g.uL } == Yes(l) } &&
    invalidUnsignedLongs.forall{ l => val g = mkGrok(l.toString); g{ implicit fail => g.uL }.isNo(g, Set(e.uL), Set(e.range, e.wrong)) } &&
    notevenNumbers.forall{ l => val g = mkGrok(l); g{ implicit fail => g.uL }.isNo(g, e.uL, e.wrong) }
  }
  
  val invalidHexLongs = Array[String]("10000000000000000", "-1", "fffffffffffffffff")
  def test_xL = mkGroks.forall{ mkGrok =>
    validSignedLongs.forall{ l => val g = mkGrok((BigInt(l) & ((BigInt(1)<<64)-1)).toString(16)); g{ implicit fail => g.xL } == Yes(l) } &&
    invalidHexLongs.forall{ l => val g = mkGrok(l); g{ implicit fail => g.xL }.isNo(g, Set(e.xL), Set(e.range, e.wrong)) } &&
    notevenNumbers.forall{ l => val g = mkGrok(l); val ans = g{ implicit fail => g.xL }; (ans == Yes(0xF) && g.position == 1) || ans.isNo(g, e.xL, e.wrong) }
  }
  
  val invalidAnyLongs = Array[String]((BigInt(Long.MinValue)-1).toString, (BigInt(1) << 64).toString, "1111111111111111111111111111199", "0x10000000000000000", "0xFffffFFFFffffFFFF")
  def test_aL = mkGroks.forall{ mkGrok =>
    validSignedLongs.forall{ l => val g = mkGrok(l.toString); g{ implicit fail => g.aL } == Yes(l) } &&
    validSignedLongs.forall{ l => val g = mkGrok((BigInt(l) & ((BigInt(1)<<64)-1)).toString); g{ implicit fail => g.aL } == Yes(l) } &&
    validSignedLongs.forall{ l => val g = mkGrok("0x" + (BigInt(l) & ((BigInt(1)<<64)-1)).toString(16)); g{ implicit fail => g.aL } == Yes(l) } &&
    validSignedLongs.forall{ l => val g = mkGrok("0X" + (BigInt(l) & ((BigInt(1)<<64)-1)).toString(16)); g{ implicit fail => g.aL } == Yes(l) } &&
    invalidAnyLongs.forall{ l => val g = mkGrok(l); g{ implicit fail => g.aL }.isNo(g, Set(e.aL, e.L, e.xL, e.uL), Set(e.range)) } &&
    notevenNumbers.forall{ l => val g = mkGrok(l); g{ implicit fail => g.aL }.isNo(g, e.aL, e.wrong) }
  }
  
  val validExplicitFloats = Array("1.0", "0", "-0", "-Infinity", "NaN", "0.1235812", "40e20", "-40e-20", "4E4", "0.000012531e+5", "9999e9999", "-9999e9999", "9e-9999", "-9e-9999")
  val invalidFloats = Array("+", "-", ".", "+.", "-.", "0e", "0e+", "0e-", ".e0")
  def test_F = mkGroks.forall{ mkGrok =>
    validExplicitFloats.forall{ f => val g = mkGrok(f); g{ implicit fail => g.F }.map(_.toString) =?= Yes(f.toFloat.toString) } &&
    (0 until 1024).map(_ => util.Random.nextInt).forall{ i => 
      val f = java.lang.Float.intBitsToFloat(i); val g = mkGrok(f.toString); g{ implicit fail => g.F }.map(_.toString) =?= Yes(f.toString)
    } &&
    invalidFloats.forall{ f => val g = mkGrok(f); g{ implicit fail => g.F }.isNo(g, Set(e.F, e.D), Set(e.end, e.wrong)) } &&
    notevenNumbers.forall{ f => val g = mkGrok(f); g{ implicit fail => g.F }.isNo(g, Set(e.F, e.D), Set(e.wrong)) }
  }
  
  /*
  // TODO - fix xF in Grok
  def test_xF = mkGroks.forall{ mkGrok =>
    validExplicitFloats.forall{ f => val g = mkGrok("%a".format(f.toFloat)); g{ implicit fail => g.xF }.map(_.toString) =?= Yes(f.toFloat.toString) } &&
    notevenNumbers.forall{ f => val g = mkGrok(f); g{ implicit fail => g.xF }.isNo(g, Set(e.xF, e.xD), Set(e.end, e.wrong)) }
  }
  */

  val validExplicitDoubles = validExplicitFloats ++ 
    Array("1e-200", "-1.19578178917985e-192", "985917982379851729857198571982982159812", "2.000002e200", "0.000000000000000000000000000000000000000000000000000000000000000000000000000000123e-3") ++
    Array("1.4764606389395946E-308", "-2.0188532403671083E-308", "1.6213519565273E-310", "-6.097892580851617E-309", "1.392111954760543E-308", "-5.59858963445749E-309") ++
    Array(Double.MaxValue.toString, Double.MinValue.toString, Double.MinPositiveValue.toString, "2.470e-324", "2.471e-324", "179769313486231580793728971405302307166001572487395108634089161737810574079057259642326644280530350389102191776040391417849536235805032710433848589497582645208959795824728567633954093335158118954813353848759795231931608806559135682943768914026291156873243967921161782282609668471618765104228873324865488158720")
  def test_D = mkGroks.forall{ mkGrok =>
    validExplicitDoubles.forall{ d => val g = mkGrok(d); g{ implicit fail => g.D }.map(_.toString) =?= Yes(d.toDouble.toString) } &&
    (0 until 8192).map(_ => util.Random.nextLong).forall{ l =>
      val d = java.lang.Double.longBitsToDouble(l); val g = mkGrok(d.toString); g{ implicit fail => g.D }.map(_.toString) =?= Yes(d.toString)
    } &&
    invalidFloats.forall{ d => val g = mkGrok(d); g{ implicit fail => g.D }.isNo(g, Set(e.D), Set(e.end, e.wrong)) } &&
    notevenNumbers.forall{ d => val g = mkGrok(d); g{ implicit fail => g.D }.isNo(g, e.D, e.wrong) }
  }
  
  // TODO - fix xD in Grok and write tests
  
  // TODO - write tests for aD once xD is fixed in Grok
  
  val validTokens = Array("", " ", "foo bar", "salmon", "incrediblylong"*10000)
  def test_tok = mkGroks.forall{ mkGrok =>
    validTokens.forall{ s => val g = mkGrok(s); g{ implicit fail => g.tok } =?= Yes(s.split(' ').headOption.getOrElse("")) }
  }
  def test_tok_n = mkGroks.forall{ mkGrok =>
    validTokens.forall{ s => val g = mkGrok(s); g{ implicit fail => g.tokLimit(2) } =?= Yes(s.split(' ').headOption.map(_.take(2)).getOrElse("")) }
  }
  def test_tok_p = mkGroks.forall{ mkGrok =>
    validTokens.forall{s => val g = mkGrok(s); g{ implicit fail => g.tokUntil(_ == 'o') } =?= Yes(s.split(' ').headOption.map(_.takeWhile(_ != 'o')).getOrElse("")) }
  }
  
  val validQuotes = Array("\"fish\"", "\"\"", """"fish are fun friends"""", """"\"\\\""""")
  val validUnquoted = Array("fish", "", "fish are fun friends", "\"\\\"")
  val invalidQuoted = Array("\"la la la", "oops")
  def test_quoted = mkGroks.forall{ mkGrok =>
    (validQuotes zip validUnquoted).forall{ case (q,u) => val g = mkGrok(q); g{ implicit fail => g.quoted } =?= Yes(u) } &&
    invalidQuoted.forall{ s => val g = mkGrok(s); g{ implicit fail => g.quoted }.isNo(g, Set(e.quote), Set(e.end, e.wrong)) }
  }
  
  def test_qtok = mkGroks.forall{ mkGrok =>
    validTokens.forall{ case q => val g = mkGrok(q); g{ implicit fail => g.qtok } == Yes(q.split(' ').headOption.getOrElse("")) } &&
    (validQuotes zip validUnquoted).forall{ case (q,u) => val g = mkGrok(q); g{ implicit fail => g.qtok } =?= Yes(u) } &&
    invalidQuoted.filter(_.startsWith("\"")).forall{ case q => val g = mkGrok(q); g{ implicit fail => g.qtok }.isNo(g, Set(e.quote), Set(e.end, e.wrong)) }
  }
  
  val validQuotedBy = Array("(fish)", """($($$$))""", "(fish are fun friends)", """("$n$r")""", "(((())))")
  val validUnquotedBy = Array("fish", "($)", "fish are fun friends", "\"\n\r\"")
  val invalidQuotedBy = Array("(fish", "oops", "((x)")
  def test_quotedBy = mkGroks.forall{ mkGrok =>
    (validQuotedBy zip validUnquotedBy).forall{ case (q,u) => val g = mkGrok(q); g{ implicit fail => g.quotedBy('(', ')', '$') } =?= Yes(u) } &&
    invalidQuotedBy.forall{ s => val g = mkGrok(s); g{ implicit fail => g.quotedBy('(', ')', '$') }.isNo(g, Set(e.quote), Set(e.end, e.wrong)) }
  }
  
  def test_qtokBy = mkGroks.forall{ mkGrok =>
    validTokens.forall{ case q => val g = mkGrok(q); g{ implicit fail => g.qtokBy('(', ')', '$') } == Yes(q.split(' ').headOption.getOrElse("")) } &&
    (validQuotedBy zip validUnquotedBy).forall{ case (q,u) => val g = mkGrok(q); g{ implicit fail => g.qtokBy('(', ')', '$') } =?= Yes(u) } &&
    invalidQuotedBy.filter(_.startsWith("(")).forall{ case q => val g = mkGrok(q); g{ implicit fail => g.qtokBy('(', ')', '$') }.isNo(g, Set(e.quote), Set(e.end, e.wrong)) } &&
    { val g = mkGrok(""); g{ implicit fail => g.qtokBy('?', '?', '!') } =?= Yes("") && g.hasToken =?= false }
  }
  
  val validBase64 = Array("bGVhcw", "bGVhc3Vy")
  val unbasedBase64 = Array("leas".getBytes, "leasur".getBytes)
  val invalidBase64 = Array("$$$$$$$")
  def test_base64 = mkGroks.forall{ mkGrok =>
    (validBase64 zip unbasedBase64).forall{ case (b,u) => val g = mkGrok(b); g{ implicit fail => g.base64 }.map(_.toSeq) =?= Yes(u.toSeq) } &&
    invalidBase64.forall{ s => val g = mkGrok(s); g{ implicit fail => g.base64 }.isNo(g, e.b64, e.wrong) }
  }
  
  def test_base64in = mkGroks.forall{ mkGrok =>
    val target = new Array[Byte](40)
    Seq(0, 10, 40 - unbasedBase64.map(_.length).max).forall{ n =>
      (validBase64 zip unbasedBase64).forall{ case (b,u) =>
        for (i <- target.indices) target(i) = 0
        val g = mkGrok(b)
        g{ implicit fail => g.base64in(target, n) }.forall{ k => target.slice(n, n+k).toSeq =?= u.toSeq }
      } &&
      invalidBase64.forall{ s => val g = mkGrok(s); g{ implicit fail => g.base64in(target, n) }.isNo(g, e.b64, e.wrong) }
    }
  }
  
  val exactThings = Array("exactly", "EXACTLY!!!", "")
  val wrongThings = Array("EXACTLY", "EX", "?")
  def test_exact = mkGroks.forall{ mkGrok =>
    exactThings.forall{ s => val g = mkGrok(s); g{ implicit fail => g.exact(s) }.isOk } &&
    exactThings.filter(_.length > 0).forall{ s => val g = mkGrok(s); g{ implicit fail => g.exact(s.charAt(0)) }.isOk } &&
    (exactThings.filter(_.length > 0) zip wrongThings).forall{ case (s,w) => val g = mkGrok(w); g{ implicit fail => g.exact(s) }.isNo(g, e.exact, e.wrong) } &&
    exactThings.filter(_.length > 0).forall{ case s => val g = mkGrok(s); g{ implicit fail => g.exact(wrongThings.last.charAt(0)) }.isNo(g, e.exact, e.wrong) }
  }
  
  val exactCaseOne = Array("exact", "EXaCt", "EXACT")
  val exactCaseTwo = Array("EXAct", "EXACT", "exact")
  val inexactCase = Array("exatc", "?", "salmon")
  def test_exactNoCase = mkGroks.forall{ mkGrok =>
    mkGrok("") match {
      case _: GrokString =>
        (exactCaseOne zip exactCaseTwo).forall{ case (a,b) => val g = mkGrok(a); g{ implicit fail => g exactNoCase b }.isOk } &&
        (exactCaseOne zip inexactCase).forall{ case (a,b) => val g = mkGrok(a); g{ implicit fail => g exactNoCase b }.isNo(g, e.exact, e.wrong) }
      case _ => true
    }
  }
  // TODO - get buffered case working
  
  val actualStrings = Array("salmon", "cod", "herring")
  val possibleStrings = Array(Array("perch", "salmon"), Array("herring", "pike", "cod"), Array("herring"))
  val impossibleStrings = Array(Array[String](), Array("salmon", "herring"), Array("?????", "!!!!!", ".....", ","))
  def test_oneOf = mkGroks.forall{ mkGrok =>
    (actualStrings zip possibleStrings).forall{ case (a,bs) => val g = mkGrok(a); g{ implicit fail => g.oneOf(bs: _*) }.isOk } &&
    (actualStrings zip impossibleStrings).forall{ case (a,bs) => val g = mkGrok(a); g{ implicit fail => g.oneOf(bs: _*) }.isNo(g, e.oneOf, e.wrong) } &&
    {
      val g = mkGrok(actualStrings.mkString("")).delimit(false)
      val all = possibleStrings.map(_.toSet).reduce(_ union _).toArray
      actualStrings.forall{ s => g{ implicit fail => g.oneOf(all: _*) } =?= Yes(s) } && !g.hasContent
    }
  }
  
  val actualCaseStrings = Array("Salmon", "COD", "herring")
  val possibleCaseStrings = Array(Array("peRCH", "saLMon"), Array("herring", "pike", "COD"), Array("hErRiNg"))
  def test_oneOfNoCase = mkGroks.forall{ mkGrok =>
    mkGrok("") match {
      case _: GrokString =>
        (actualCaseStrings zip possibleCaseStrings).forall{ case (a,bs) => val g = mkGrok(a); g{ implicit fail => g.oneOfNoCase(bs: _*) }.isOk } &&
        (actualCaseStrings zip impossibleStrings).forall{ case (a,bs) => val g = mkGrok(a); g{ implicit fail => g.oneOfNoCase(bs: _*) }.isNo(g, e.oneOf, e.wrong) } &&
        {
          val g = mkGrok(actualStrings.mkString("")).delimit(false)
          val all = possibleStrings.map(_.toSet).reduce(_ union _).toArray
          actualStrings.forall{ s => g{ implicit fail => g.oneOf(all: _*) }.map(_.toLowerCase) =?= Yes(s.toLowerCase) } && !g.hasContent
        }
      case _ => true
    }
  }
  // TODO - get buffered case working
  
  val stringSourceOfBytes = "This is a string, and even with delimiters you just grab bytes from it."
  def test_bytes = mkGroks.forall{ mkGrok =>
    Seq(0, 1, 4, 12, stringSourceOfBytes.length).forall{ n =>
      val g = mkGrok(stringSourceOfBytes)
      g{ implicit fail => g.bytes(n).toSeq } =?= Yes(stringSourceOfBytes.map(_.toByte).take(n))
    } &&
    {
      val g = mkGrok(stringSourceOfBytes);
      g{implicit fail => g.bytes(stringSourceOfBytes.length+1)}.isNo(g, e.bin, e.end)
    }
  }
  
  def test_bytesIn = mkGroks.forall{ mkGrok =>
    val target = new Array[Byte](stringSourceOfBytes.length+20)
    Seq(0, 1, 4, 12, stringSourceOfBytes.length).forall{ n =>
      Seq(0, 1, 8, 20).forall{ k =>
        val g = mkGrok(stringSourceOfBytes)
        for (i <- target.indices) target(i) = 0
        g{ implicit fail => g.bytesIn(n, target, k) }
        target.drop(k).take(n).toSeq =?= stringSourceOfBytes.map(_.toByte).take(n)
      }
    } &&
    Seq(0, 1, 8, 19).forall{ k =>
      val g = mkGrok(stringSourceOfBytes)
      g{ implicit fail => g.bytesIn(stringSourceOfBytes.length+1, target, k) }.isNo(g, e.bin, e.end)
    } &&
    { val g = mkGrok(stringSourceOfBytes); g{ implicit fail => g.bytesIn(12, target, target.length-6) }.isNo(g, e.bin, e.range) }
  }
  
  def test_customError = mkGroks.forall{ mkGrok =>
    val g = mkGrok(" ")
    g.customError.whyError == e.wrong && g.customError.whoError == e.custom &&
    g.customError("salmon").description == "salmon"
  }
  
  val positionableString = """1 22 333 4444 55555 666666 7777777"""
  def positionIndices = Seq(0, 2, 5, 9, 14, 20, 27, positionableString.length)
  def test_position = mkGroks.forall{ mkGrok =>
    val g = mkGrok(positionableString)
    val i = positionIndices.iterator
    g.position =?= i.next && i.forall{ _ =?= { g{ implicit fail => g.I }; g.position } } && g.position =?= positionableString.length
  }
  
  val emptyAfter = Array(
    "" -> (0,1),
    " " -> (1,2),
    "foo" -> (1,1),
    "aosufq" -> (1,1),
    "reallylong"*100 -> (1,1),
    "  " -> (2,3),
    "foo bar" -> (2,2),
    " salmon" -> (2,2),
    "   " -> (3,4),
    "foo bar baz" -> (3,3),
    " salmon " -> (2,3),
    " perch  " -> (3,4)
  )
  def test_empty = mkGroks.forall{ mkGrok =>
    emptyAfter.forall{ case (s, (nc, nt)) =>
      val g = mkGrok(s)
      (1 to nc).forall{ _ => val ans = g.hasContent && g.hasToken; g.trySkip; ans =?= true } &&
      g.hasContent.expectHere(s"content for '$s'")(false) &&
      (if (nc < nt) (1 to (nt-nc)).forall{ _ => val ans = g.hasToken; g.trySkip; ans =?= true } else true) &&
      g.hasToken.expectHere(s"token for '$s'")(false)
    }
  }
  
  val trimmableStrings = Array("foo", " bar", "                             baz")
  val test_trim = mkGroks.forall{ mkGrok =>
    trimmableStrings.forall{ s => val g = mkGrok(s); g{ implicit fail => g.trim } =?= Yes(s.takeWhile(_ == ' ').length) } &&
    trimmableStrings.forall{ s => val g = mkGrok(s); g{ implicit fail => g.trimmed.oTok }.toOption.flatten =?= Some(s.trim) }
  }
  
  val test_trySkip = mkGroks.forall{ mkGrok =>
    (validSkipOne ++ validSkipThree).forall{ s =>
      val g = mkGrok(s)
      val h = mkGrok(s)
      Iterator.continually(g.trySkip).takeWhile(_ == true).forall{ _ => h{ implicit fail => h.skip }.isOk } && h{ implicit fail => h.skip }.isNo(h, e.tok, e.end)
    } &&
    (validSkipOne ++ validSkipThree).forall{ s => (1 to 4).forall{ n =>
      val g = mkGrok(s)
      val h = mkGrok(s)
      Iterator.continually(g.trySkip(n) == n).takeWhile(_ == true).forall{ _ => h{ implicit fail => h.skip(n) }.isOk } && h{ implicit fail => h.skip(n) }.isNo(h, e.tok, e.end)
    }}
  }
  
  val test_oZ = mkGroks.forall{ mkGrok =>
    (trueAnyBool ++ falseAnyBool ++ invalidAnyBool).forall{ s =>
      val g = mkGrok(s)
      val h = mkGrok(s)
      g.oZ == h{ implicit fail => h.aZ }.toOption
    }
  }
  
  val test_oC = mkGroks.forall{ mkGrok =>
    (validCharacters ++ invalidCharacters).forall{ s =>
      val g = mkGrok(s)
      val h = mkGrok(s)
      g.oC == h{ implicit fail => h.C }.toOption
    }
  }
  
  val test_oI = mkGroks.forall{ mkGrok =>
    (
      validSignedInts.map(_.toString) ++
      validSignedInts.map(i => (i & 0xFFFFFFFFL).toString) ++
      validSignedInts.map(i => i.toHexString).flatMap(s => Seq("0x", "0X").map(_ + s)) ++
      invalidAnyInts ++
      notevenNumbers
    ).forall{ s =>
      val g = mkGrok(s)
      val h = mkGrok(s)
      g.oI == h{ implicit fail => h.aI }.toOption
    }
  }
  
  val test_oL = mkGroks.forall{ mkGrok =>
    (
      validSignedLongs.map(_.toString) ++
      validSignedLongs.map(l => BigInt(l) & ((BigInt(1)<<64)-1)).map(_.toString) ++
      validSignedLongs.map(_.toHexString).flatMap(s => Seq("0x", "0X").map(_ + s)) ++
      invalidAnyLongs ++
      notevenNumbers
    ).forall{ s =>
      val g = mkGrok(s)
      val h = mkGrok(s)
      g.oL == h{ implicit fail => h.aL }.toOption
    }
  }
  
  val test_oD = mkGroks.forall{ mkGrok =>
    (validExplicitDoubles ++ invalidFloats ++ notevenNumbers).forall{ s =>
      val g = mkGrok(s)
      val h = mkGrok(s)
      g.oD.map(_.toString) =?= h{ implicit fail => h.D.toString }.toOption
    }
  }
  
  def test_oTok = mkGroks.forall{ mkGrok =>
    validTokens.forall{ s =>
      val g = mkGrok(s)
      val h = mkGrok(s)
      Iterator.continually(g.oTok).takeWhile(_.isDefined).forall{ _ == h{ implicit fail => h.tok }.toOption } && h{ implicit fail => h.tok }.isNo(h, e.tok, e.end)
    }
  }
  
  def test_indexTok = mkGroks.forall{ mkGrok =>
    validTokens.forall{ s =>
      val g = mkGrok(s)
      val h = mkGrok(s)
      Iterator.continually(g.indexTok).takeWhile(_ != -1L).forall{ l => Some(s.substring(l.asInts.i0, l.asInts.i1)) =?= h{ implicit fail => h.tok }.toOption } &&
      h{ implicit fail => h.tok }.isNo(h, e.tok, e.end)
    }
  }
  
  def test_oQuotedBy = mkGroks.forall{ mkGrok =>
    (validQuotes zip validUnquoted).forall{ case (s,u) =>
      val g = mkGrok(s)
      g.oQuotedBy('"', '"', '\\') =?= Some(u)
    } &&
    (validQuotedBy zip validUnquotedBy).forall{ case (s,u) =>
      val g = mkGrok(s)
      g.oQuotedBy('(', ')', '$') =?= Some(u)
    }
  }
  
  def test_tryExact = mkGroks.forall{ mkGrok =>
    val sources = ((exactThings ++ exactThings) zip (exactThings ++ wrongThings))
    sources.forall { case (s,w) =>
      val g = mkGrok(s)
      val h = mkGrok(s)
      g.tryExact(w) == h{ implicit fail => h.exact(w) }.isOk
    } &&
    sources.forall{ case (s,w) =>
      val g = mkGrok(s)
      val h = mkGrok(s)
      w.length == 0 || g.tryExact(w.charAt(0)) == h{ implicit fail => h.exact(w.charAt(0)) }.isOk
    }
  }
  
  def test_peek = mkGroks.forall{ mkGrok =>
    (validCharacters ++ invalidCharacters).forall{ s =>
      val g = mkGrok(s)
      val h = mkGrok(s)
      g.peek match {
        case i if i < 0 => h.oC.isEmpty
        case i => if (i.toChar.isWhitespace) h.oC.isEmpty else h.oC.map(_.toInt) =?= Some(i)
      }
    } &&
    {
      val g = mkGrok(validCharacters.mkString(" "))
      val i = new Iterator[(Int, Option[Char])] { def hasNext = g.peek >= 0; def next = (g.peek, g.oC) }
      i.forall{ case (x, o) => if (x.toChar.isWhitespace) o.isEmpty else o.map(_.toInt) =?= Some(x) }
    }
  }
  
  val consistentlyPeekable = "t x q f ? . ! \u0002"
  def test_peekAt = mkGroks.forall{ mkGrok =>
    (validCharacters ++ invalidCharacters).forall{ s => val g = mkGrok(s); g.peek == g.peekAt(0) } &&
    {
      val source = consistentlyPeekable
      val g = mkGrok(source)
      (Iterator(Option("")) ++ Iterator.continually(g.oTok).takeWhile(_.isDefined) ++ Iterator(Option(""))).forall{ o =>
        (-source.length-1 to source.length+1).forall{ k =>
          val i = (g.position + k).toInt
          val j = g.peekAt(k)
          if (i < 0 || i >= source.length) j =?= -1 else source.charAt(i) =?= j
        }
      }
    }
  }

  def test_peekIndices = mkGroks.forall{ mkGrok =>
    validTokens.forall{ s =>
      val g = mkGrok(s)
      Iterator.continually((g.peekIndices, g.oTok)).takeWhile(_ != (-1L, None)).forall{ case (l, o) => o =?= Some(s.substring(l.asInts.i0, l.asInts.i1)) }
    }
  }
  
  def test_peekTok = mkGroks.forall{ mkGrok =>
    validTokens.forall{ s =>
      val g = mkGrok(s)
      Iterator.continually((Option(g.peekTok), g.oTok)).takeWhile(_ != (None, None)).forall{ case (t, o) => t == o }
    }
  }
  
  val notAllIntegers = "1234 foo ???"
  def test_context = mkGroks.forall{ mkGrok =>
    val g = mkGrok(notAllIntegers)
    val h = mkGrok(notAllIntegers)
    val a = h{implicit fail => (h.I, h.I)}
    var contextString = "early"
    val b = g{ implicit fail => g.context(contextString){ contextString = "mid"; (g.I, { contextString = "late"; g.I }) } }
    !a.isOk && !b.isOk && a.swap.exists(ae => b.swap.exists(be =>
      be.description =?= "late" && ae.position =?= (be.position +: be.suberrors.map(_.position)).max
    ))
  }
  
  def test_attempt = mkGroks.forall{ mkGrok =>
    val g = mkGrok(notAllIntegers)
    val h = mkGrok(notAllIntegers)
    val a = h{ implicit fail => (h.I: Any, h.tok: Any, h.tok: Any) }
    val b = g{ implicit fail => (g.attempt(g.I).yesOr(_ => g.tok), g.attempt(g.I).yesOr(_ => g.tok), g.attempt(g.I).yesOr(_ => g.tok)) }
    a =?= b
  }
  
  def test_tangent = mkGroks.forall{ mkGrok =>
    val g = mkGrok(notAllIntegers)
    val h = mkGrok(notAllIntegers)
    val a = h{ implicit fail => (h.I, h.tok, h.C, h.tok, true) }
    val b = g{ implicit fail =>
      val w = g.tangent{ g.tok }
      val x = g.tangent{ g.I }
      var tf = x == g.I && w == x.toString
      val y = g.tangent{ g.C }
      val z = g.tok
      tf = tf && z.take(1) == y.toString
      val v = g.tangent{ g.tok }
      val u2 = g.tangent{ g.C }
      val u = g.C
      tf = tf && u2 == u && v.take(1) == u.toString
      g.tangent{ g exact "??" }
      val t = g.tangent{ g.C }
      val s = g.tok
      tf = tf && s.take(1) ==  t.toString
      (x, z, u, s, tf)
    }
    a =?= b
  }
  
  def test_each = mkGroks.forall{ mkGrok =>
    val g = mkGrok(validExplicitDoubles.mkString(" "))
    g{ implicit fail => g.each{ g.D.toString }.toSeq } =?= Yes(validExplicitDoubles.map(_.toDouble.toString).toSeq) &&
    g{ implicit fail => g.each{ g.D }.toSeq } =?= Yes(Seq()) &&
    {
      val h = mkGrok(notAllIntegers)
      h{ implicit fail => h.each{ h.I }.toSeq }.isNo(h, e.I, e.wrong)
    } &&
    {
      val h = mkGrok(" one two three ")
      h{ implicit fail => h.each{ h.tok }.toSeq } =?= Yes(Seq("", "one", "two", "three", ""))
    } &&
    {
      // Make sure we advance even if we don't do anything per token
      val h = mkGrok(" one two three ")
      h{ implicit fail => h.each{ () }.length } =?= Yes(5)
    }
  }
  
  def test_filterMap = mkGroks.forall{ mkGrok =>
    val g = mkGrok(validExplicitDoubles.mkString(" "))
    g{ implicit fail => g.filterMap{ g.D }{_ > 0}{ _.toInt }.toSeq } =?= Yes(validExplicitDoubles.map(_.toDouble).filter(_ > 0).map(_.toInt).toSeq)
  }
  
  def test_grokEach = mkGroks.forall{ mkGrok =>
    {
      val g = mkGrok(validExplicitDoubles.mkString(" "))
      g.grokEach('.'){ implicit fail => g.tok }.map(_.toSeq) =?= Yes(validExplicitDoubles.map(_.split('.').take(1).mkString).toSeq)
    } &&
    {
      val g = mkGrok(notAllIntegers)
      g.grokEach('.'){ implicit fail => g.I } match {
        case No((y,n)) =>
          y.toSeq == Seq(1234) &&
          n.map(_.position).toSeq =?= n.map(_.position).sorted.toSeq &&
          n.length == 2          
        case _ => false
      }
    }
  }
  
  def test_delimit = mkGroks.forall{ mkGrok =>
    val common = "12fish yummy deal!  Deal!!!";
    {
      val g = mkGrok(common).delimit(true)
      val h = mkGrok(common).delimit(false)
      g{ implicit fail => g.I }.isNo(g, e.I, e.delim) &&
      h{ implicit fail => h.I } == Yes(12) &&
      h{ implicit fail => h.delimit(true).skip(2) exact "deal" }.isNo(h, e.exact, e.delim)
    } &&
    {
      val g = mkGrok(common).delimit('e')
      val h = mkGrok(common).delimit(new CharDelim('e'))
      g{ implicit fail => Seq(g.tok, g.tok, g.tok) } =?= Yes(common.split('e').toSeq) &&
      h{ implicit fail => Seq(h.tok, h.tok, h.tok) } =?= Yes(common.split('e').toSeq)
    } &&
    {
      val g = mkGrok(common).delimit(0, 'm')
      val h = mkGrok(common).delimit(1, 'm')
      val i = mkGrok(common).delimit(0, new CharDelim('m'))
      g{ implicit fail => (g.tok, g.tok) } == Yes((common.takeWhile(_ != 'm'), common.reverse.takeWhile(_ != 'm').reverse)) &&
      h{ implicit fail => (h.tok, h.tok, h.tok) } == Yes((common.takeWhile(_ != 'm'), "", common.reverse.takeWhile(_ != 'm').reverse)) &&
      i{ implicit fail => (i.tok, i.tok) } == Yes((common.takeWhile(_ != 'm'), common.reverse.takeWhile(_ != 'm').reverse))
    } &&
    {
      val g = mkGrok(common).delimit(true, 'e')
      val h = mkGrok(common).delimit(true, new CharDelim('e'))
      g{ implicit fail => g.I }.isNo(g, e.I, e.delim) &&
      h{ implicit fail => h.I }.isNo(h, e.I, e.delim)
    } &&
    {
      val g = mkGrok(common).delimit(0, ' ')
      val h = mkGrok(common).delimit(0, new CharDelim(' '))
      g{ implicit fail => Seq(g.tok, g.tok, g.tok, g.tok) } == Yes(common.split(" +").toSeq)
      h{ implicit fail => Seq(h.tok, h.tok, h.tok, h.tok) } == Yes(common.split(" +").toSeq)
    } &&
    {
      val g = mkGrok(common).delimit(true, 0, 'm')
      val h = mkGrok(common).delimit(true, 0, new CharDelim(' '))
      g{ implicit fail => g.I }.isNo(g, e.I, e.delim)
      h{ implicit fail => h.I }.isNo(h, e.I, e.delim)
    } &&
    {
      val g = mkGrok(common).delimit(true, 0, new TerminatedDelim(new CharDelim('!'), new CharDelim(' ')))
      val h = mkGrok(common).delimit(true, 0, new TerminatedDelim(new CharDelim('!'), new CharDelim(' '))).delimit('y')
      val i = mkGrok(common).delimit(true, 0, new TerminatedDelim(new CharDelim('!'), new CharDelim(' '))).freshDelimiter('y')
      g.oTok =?= Some(common.takeWhile(_ != ' ')) &&
      h.oTok =?= Some(common.takeWhile(_ != ' ')) &&
      i.oTok =?= Some(common.takeWhile(_ != 'y'))
    }
  }
  
  def test_GrokString_input = {
    val g = Grok("initial input")
    g{ implicit fail => (g.tok, g.input("input finalized", 6, 11).tok) } =?= Yes(("initial", "final"))
  }
  
  def test_GrokBuffer_input = {
    val g = Grok text "initial input".getBytes
    g{ implicit fail => (g.tok, g.input("input finalized".getBytes, 6, 11).tok) } =?= Yes(("initial", "final"))
  }
  
  def main(args: Array[String]) { typicalMain(args) }
}

class Test_Grok_from_JUnit {
  @org.junit.Test
  def test() { Test_Grok.main(Array()) }
}