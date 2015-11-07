// This file is distributed under the BSD 3-clause license.  See file LICENSE.
// Copyright (c) 2015 Rex Kerr and Calico Life Sciences.

package kse.jsonic.parsers

import kse.jsonic.ast._

trait JsonParser[A] { def parse(input: A): JsResult }

class StringParser extends JsonParser[String] {
  private[this] var idx = 0

  def parse(input: String): JsResult = parseVal(input, 0)

  private def parseVal(input: String, index: Int): JsResult = {
    var i = index
    var c: Char = 0
    while (i < input.length && { c = input.charAt(index); c < 21 && (c == ' ' || c == '\n' || c == '\r' || c == '\t')}) i += 1
    if (i >= input.length) JsError("no value: end of input", index, index, None)
    else if (c == '"') parseStr(input, i+1)
    else if (c == '[') parseArr(input, i+1)
    else if (c == '{') parseObj(input, i+1)
    else if (c == '-') parseNum(input, i+1, true)
    else if (c >= '0' && c <= '9') parseNum(input, i, false)
    else if (c == 'n') parseNull(input, i+1)
    else if (c == 't') parseTrue(input, i+1)
    else if (c == 'f') parseFalse(input, i+1)
    else JsError("invalid character: '" + c + "'", i, i, None)
  }

  private def parseNull(input: String, index: Int): JsResult = {
    if (index+3 < input.length && input.charAt(index) == 'u' && input.charAt(index+1) == 'l' && input.charAt(index+2) == 'l') { idx = index+3; JsNull }
    else JsError("Expected 'null' but found '"+input.substring(index-1, index+3), index-1, index+3, None)
  }

  private def parseTrue(input: String, index: Int): JsResult = {
    if (index+3 < input.length && input.charAt(index) == 'r' && input.charAt(index+1) == 'u' && input.charAt(index+2) == 'e') { idx = index+3; JsTrue }
    else JsError("Expected 'true' but found '"+input.substring(index-1, index+3), index-1, index+3, None)
  }

  private def parseFalse(input: String, index: Int): JsResult = {
    if (index+4 < input.length && input.charAt(index) == 'a' && input.charAt(index+1) == 'l' && input.charAt(index+2) == 's' && input.charAt(index+3) == 'e') {
      idx = index+4
      JsTrue
    }
    else JsError("Expected 'false' but found '"+input.substring(index-1, index+4), index-1, index+3, None)
  }

  private def parseStr(input: String, index: Int): JsResult = {
    val i = parseSimpleStr(input, index)
    if (i >= 0) { idx = i + 1; JsStr(input.substring(index, i)) }
    else parseComplexStr(input, index, -i-1)
  }
  
  private def parseSimpleStr(input: String, index: Int): Int = {
    var i = index
    var c: Char = 0
    while (i < input.length && { c = input.charAt(i); c != '"' && c != '\\' }) i += 1
    if (c == '"') i else -i-1
  }

  private def hexifyChar(c: Char): Int = hexifyLowerChar(c | 0x20)

  private def hexifyLowerChar(c: Int): Int =
    if (c >= '0' && c <= '9') c - '0' else if (c >= 'a' && c <= 'f') c - 87 else -1

  private def parseComplexStr(input: String, index: Int, cleanUntil: Int): JsResult = {
    val N = input.length
    var n = (0xFFFFFFFF >>> math.min(28, java.lang.Integer.numberOfLeadingZeros(cleanUntil - index))) & 0x7FFFFFFE
    var j = 0
    var buffer = new Array[Char](n)
    var i0 = index
    var iN = cleanUntil
    var c: Char = '\\'
    do {
      if (i0 < iN) {
        if (iN - i0 > n - j) {
          n = (n << 1) | 0x2
          buffer = java.util.Arrays.copyOf(buffer, n)
        }
        input.getChars(i0, iN, buffer, j)
        j += (iN - i0)
        i0 = iN
      }
      if (c == '"') {
        idx = i0+1
        return JsStr(new String(buffer, 0, n))
      }
      while (c == '\\') {
        i0 += 1
        if (i0 < N) {
          val q = input.charAt(i0) match {
            case 'n' => '\n'
            case 'r' => '\r'
            case 't' => '\t'
            case 'u' =>
              if (i0 >= N - 4) return JsError("string ends mid-unicode-escape", index, N, None)
              val h = (hexifyChar(input.charAt(i0+1)) << 12) | 
                      (hexifyChar(input.charAt(i0+2)) << 8) | 
                      (hexifyChar(input.charAt(i0+3)) << 4) | 
                      hexifyChar(input.charAt(i0+4))
              if (h < 0) return JsError("non-hex value in unicode escape", index, i0+1, None)
              i0 += 4
              h.toChar
            case 'f' => '\f'
            case 'b' => '\b'
            case x => 
              if (x == '"' || x == '/' || x == '\\') x
              else return JsError("invalid quoted character '" + x + "'", index, i0, None)
          }
          i0 += 1
          if (i0 < N) c = input.charAt(i0)
          if (j >= n) {
            n = (n << 1) | 0x2
            buffer = java.util.Arrays.copyOf(buffer, n)
          }
          buffer(j) = q
          j += 1
        }
        else return JsError("string ends mid-escape", index, i0, None)
        if (i0 < N) c = input.charAt(i0)
        else return JsError("no closing quote on string", index, i0, None)
      }
      if (c == '"') {
        idx = i0+1
        return JsStr(new String(buffer, 0, n))
      }
      if (i0+1 < N) {
        iN = parseSimpleStr(input, i0+1)
        if (iN < 0) iN = -iN-1
      }
      else i0 = N
    } while (i0 < N)
    JsError("no closing quote on string", index, iN, None)
  }

  private def parseNum(input: String, index: Int, signed: Boolean) = if (index >= input.length) JsError("unfinished number", index, index, None) else {
    var dadp = 0  // How many of our digits are after the decimal point?
    var nnzd = 0  // How many digits did we read?
    var digits = 0L
    var i = index
    var c = input.charAt(i)
    val N = input.length
    if (c > '0' && c <= '9') {
      digits = c - '0'
      val M = math.min(i+15, N)
      i += 1
      while (i < M && { c = input.charAt(i); c >= '0' && c <= '9' }) { i += 1; digits = digits*10 + (c - '0') }
      nnzd = i - index
    }
    ???
  }

  private def parseArr(input: String, index: Int) = ???

  private def parseObj(input: String, index: Int) = ???
}
