package kse.eio

import language.implicitConversions

import java.lang.Character
import kse.coll.packed._

trait Delimiter {
  def apply(s: String, i0: Int, iN: Int, n: Int): Int
  def _tok(s: String, i0: Int, iN: Int, n: Int): Long
  def tok_(s: String, i0: Int, iN: Int, n: Int): Long
  def apply(ab: Array[Byte], i0: Int, iN: Int, n: Int): Int
  def _tok(ab: Array[Byte], i0: Int, iN: Int, n: Int): Long
  def tok_(ab: Array[Byte], i0: Int, iN: Int, n: Int): Long
  
  def terminatedBy(d2: Delimiter) = new TerminatedDelim(this, d2)
}

final class CharDelim(private val c: Char) extends Delimiter {
  final def apply(s: String, i0: Int, iN: Int, n: Int): Int = {
    if (i0 >= iN) -1-iN
    else {
      var i = i0
      var k = n
      while (i < iN && k > 0 && s.charAt(i) == c) { i += 1; k -= 1 }
      i
    }
  }
  final def apply(ab: Array[Byte], i0: Int, iN: Int, n: Int): Int = {
    if (i0 >= iN) -1-iN
    else {
      var i = i0
      var k = n
      while (i < iN && k > 0 && ab(i) == c) { i += 1; k -= 1 }
      i
    }
  }
  final def _tok(s: String, i0: Int, iN: Int, n: Int): Long = {
    var i = i0
    var k = n
    while (i < iN && k > 0 && s.charAt(i) == c) { i += 1; k -= 1 }
    if (i >= iN) {
      return if (i == i0) (-1-iN packII -1-iN).L else (i packII i).L
    }
    var j = i+1
    while (j < iN && s.charAt(j) != c) j += 1
    (i packII j).L
  }
  final def _tok(ab: Array[Byte], i0: Int, iN: Int, n: Int): Long = {
    var i = i0
    var k = n
    while (i < iN && k > 0 && ab(i) == c) { i += 1; k -= 1 }
    if (i >= iN) {
      return if (i == i0) (-1-iN packII -1-iN).L else (i packII i).L
    }
    var j = i+1
    while (j < iN && ab(j) != c) j += 1
    (i packII j).L
  }
  final def tok_(s: String, i0: Int, iN: Int, n: Int): Long = {
    var i = i0
    while (i < iN && s.charAt(i) != c) i += 1
    if (i >= iN) {
      return if (i == i0) (-1-iN packII -1-iN).L else (i packII i).L
    }
    var j = i+1
    var k = n-1
    while (j < iN && k > 0 && s.charAt(j) == c) j += 1
    (i packII j).L
  }
  final def tok_(ab: Array[Byte], i0: Int, iN: Int, n: Int): Long = {
    var i = i0
    while (i < iN && ab(i) != c) i += 1
    if (i >= iN) {
      return if (i == i0) (-1-iN packII -1-iN).L else (i packII i).L
    }
    var j = i+1
    var k = n-1
    while (j < iN && k > 0 && ab(j) == c) j += 1
    (i packII j).L
  }
}

final class WhiteDelim extends Delimiter  {
  @inline final def whiteByte(b: Byte) = ((b & 0xFF) <= 32) && ((1L << b) & 0x1f0003e00L) != 0
  @inline final def darkByte(b: Byte) = ((b & 0xFF) > 32) || ((1L << b) & 0x1f0003e00L) == 0
  final def apply(s: String, i0: Int, iN: Int, n: Int): Int = {
    if (i0 >= iN) -1-iN
    else {
      var i = i0
      var k = n
      while (i < iN && k > 0 && Character.isWhitespace(s.charAt(i))) { i += 1; k -= 1 }
      i
    }
  }
  final def apply(ab: Array[Byte], i0: Int, iN: Int, n: Int): Int = {
   if (i0 >= iN) -1-iN
   else {
     var i = i0
     var k = n
     while (i < iN && k > 0 && whiteByte(ab(i))) { i += 1; k -= 1 }
     i
   }
  }
  final def _tok(s: String, i0: Int, iN: Int, n: Int): Long = {
    var i = i0
    var k = n
    while (i < iN && k > 0 && Character.isWhitespace(s.charAt(i))) { i += 1; k -= 1 }
    if (i >= iN) {
      return if (i == i0) (-1-iN packII -1-iN).L else (i packII i).L
    }
    var j = i+1
    while (j < iN && !Character.isWhitespace(s.charAt(j))) j += 1
    (i packII j).L
  }
  final def _tok(ab: Array[Byte], i0: Int, iN: Int, n: Int): Long = {
    var i = i0
    var k = n
    while (i < iN && k > 0 && whiteByte(ab(i))) { i += 1; k -= 1 }
    if (i >= iN) {
      return if (i == i0) (-1-iN packII -1-iN).L else (i packII i).L
    }
    var j = i+1
    while (j < iN && darkByte(ab(j))) j += 1
    (i packII j).L
  }
  final def tok_(s: String, i0: Int, iN: Int, n: Int): Long = {
    var i = i0
    while (i < iN && !Character.isWhitespace(s.charAt(i))) i += 1
    if (i >= iN) {
      return if (i == i0) (-1-iN packII -1-iN).L else (i packII i).L
    }
    var j = i+1
    var k = n-1
    while (j < iN && k > 0 && Character.isWhitespace(s.charAt(j))) j += 1
    (i packII j).L
  }
  final def tok_(ab: Array[Byte], i0: Int, iN: Int, n: Int): Long = {
    var i = i0
    while (i < iN && darkByte(ab(i))) i += 1
    if (i >= iN) {
      return if (i == i0) (-1-iN packII -1-iN).L else (i packII i).L
    }
    var j = i+1
    var k = n-1
    while (j < iN && k > 0 && whiteByte(ab(j))) j += 1
    (i packII j).L
  }
}

final class LineDelim extends Delimiter  {
  final def apply(s: String, i0: Int, iN: Int, n: Int): Int = {
    if (i0 >= iN) -1-iN
    else {
      var i = i0
      var k = n
      while (i < iN && k > 0) {
        val c = s.charAt(i);
        if (c == '\n') { i += 1; k -= 1 }
        else if (c == '\r') {
          i += 1
          if (i >= iN || s.charAt(i) != '\n') k -= 1
        }
        else return i
      }
      i
    }
  }
  final def apply(ab: Array[Byte], i0: Int, iN: Int, n: Int): Int = {
    if (i0 >= iN) -1-iN
    else {
      var i = i0
      var k = n
      while (i < iN && k > 0) {
        val c = ab(i);
        if (c == '\n') { i += 1; k -= 1 }
        else if (c == '\r') {
          i += 1
          if (i >= iN || ab(i) != '\n') k -= 1
        }
        else return i
      }
      i
    }
  }
  final def _tok(s: String, i0: Int, iN: Int, n: Int): Long = {
    val i1 = apply(s, i0, iN, n)
    if (i1 >= iN) {
      return if (i1 == i0) (-1-iN packII -1-iN).L else (i1 packII i1).L
    }
    var j = i1+1
    while (j < iN && {val c = s.charAt(j); c != '\n' && c != '\r'}) j += 1
    (i1 packII j).L
  }
  final def _tok(ab: Array[Byte], i0: Int, iN: Int, n: Int): Long = {
    val i1 = apply(ab, i0, iN, n)
    if (i1 >= iN) {
      return if (i1 == i0) (-1-iN packII -1-iN).L else (i1 packII i1).L
    }
    var j = i1+1
    while (j < iN && {val c = ab(j); c != '\n' && c != '\r'}) j += 1
    (i1 packII j).L
  }
  final def tok_(s: String, i0: Int, iN: Int, n: Int): Long = {
    var i = i0
    while (i < iN &&  {val c = s.charAt(i); c != '\n' && c != '\r'}) i += 1
    if (i >= iN) {
      return if (i == i0) (-1-iN packII -1-iN).L else (i packII i).L
    }
    val i1 = apply(s, i, iN, n)
    (i packII i1).L
  }
  final def tok_(ab: Array[Byte], i0: Int, iN: Int, n: Int): Long = {
    var i = i0
    while (i < iN &&  {val c = ab(i); c != '\n' && c != '\r'}) i += 1
    if (i >= iN) {
      return if (i == i0) (-1-iN packII -1-iN).L else (i packII i).L
    }
    val i1 = apply(ab, i, iN, n)
    (i packII i1).L
  }
}

final class TerminatedDelim(tokenizer: Delimiter, terminator: Delimiter) extends Delimiter {
  private[this] var cachedData: AnyRef = null
  private[this] var cachedStart: Int = Int.MaxValue
  private[this] var cachedEnd: Int = Int.MaxValue
    
  private def loadCaches(s: String, i0: Int, iN: Int) {
    if (!(s eq cachedData) || (cachedStart > i0) || (cachedEnd < i0)) {
      cachedData = s
      cachedStart = i0
      val term = terminator.tok_(s, i0, iN, 1).inLong
      cachedEnd = term.i0 match { case -1 => i0; case x if x == term.i1 => iN+1; case x => x }
    }
  }
  private def loadCaches(ab: Array[Byte], i0: Int, iN: Int) {
    if (!(ab eq cachedData) || (cachedStart > i0)) {
      cachedData = ab
      cachedStart = i0
      val term = terminator.tok_(ab, i0, iN, 1).inLong
      cachedEnd = term.i0 match { case -1 => i0; case x if x == term.i1 => iN+1; case x => x }
    }
  }
    
  def apply(s: String, i0: Int, iN: Int, n: Int): Int = {
    if (i0 >= iN) { cachedData = null; return -1-iN }
    loadCaches(s, i0, iN)
    tokenizer(s, i0, math.min(iN, cachedEnd), n)
  }
  def apply(ab: Array[Byte], i0: Int, iN: Int, n: Int): Int = {
    if (i0 >= iN) { cachedData = null; return -1-iN }
    loadCaches(ab, i0, iN)
    tokenizer(ab, i0, math.min(iN, cachedEnd), n)
  }
  
  def _tok(s: String, i0: Int, iN: Int, n: Int): Long = {
    if (i0 >= iN) { cachedData = null; return -1-iN }
    loadCaches(s, i0, iN)
    val ans = tokenizer._tok(s, i0, math.min(iN, cachedEnd), n)
    if (ans.inLong.i0 == cachedEnd) { cachedData = null; return -1-cachedEnd }
    ans
  }
  def _tok(ab: Array[Byte], i0: Int, iN: Int, n: Int): Long = {
    if (i0 >= iN) { cachedData = null; return -1-iN }
    loadCaches(ab, i0, iN)
    val ans = tokenizer._tok(ab, i0, math.min(iN, cachedEnd), n)
    if (ans.inLong.i0 == cachedEnd) { cachedData = null; return -1-cachedEnd }
    ans
  }
  
  def tok_(s: String, i0: Int, iN: Int, n: Int): Long = {
    if (i0 >= iN) { cachedData = null; return -1-iN }
    loadCaches(s, i0, iN)
    tokenizer.tok_(s, i0, math.min(iN, cachedEnd), n)
  }
  def tok_(ab: Array[Byte], i0: Int, iN: Int, n: Int): Long = {
    if (i0 >= iN) { cachedData = null; return -1-iN }
    loadCaches(ab, i0, iN)
    tokenizer.tok_(ab, i0, math.min(iN, cachedEnd), n)
  }
}

object Delimiter {
  val zero = new CharDelim(0: Char)
  val comma = new CharDelim(',')
  val semi = new CharDelim(';')
  val colon = new CharDelim(':')
  val space = new CharDelim(' ')
  val tab = new CharDelim('\t')
  val white = new WhiteDelim
  val newline = new LineDelim
}
 