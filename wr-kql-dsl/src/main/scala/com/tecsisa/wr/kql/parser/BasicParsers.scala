package com.tecsisa.wr
package kql
package parser

import fastparse.all._

trait BasicParsers extends Helpers {

  // A parser for optional spaces
  val space = P(CharsWhile(Whitespace).?)

  // A parser for one space at least
  val oneSpaceAtLeast = P(" " ~ space)

  // A parser for digits
  val digits = P(CharsWhile(Digits))

  // A parser for integral numbers
  val integral = P("0" | CharIn('1' to '9') ~ digits.?)

  // A parser for integer numbers
  val integer = P("-".? ~ integral).!.map(_.toInt)

  // A parser for double numbers (IEEE 754 floating point)
  val double = P("-".? ~ integral ~ "." ~ integral.rep(min = 1, max = 16)).!.map(_.toDouble)

  // A parser for dates (yyyy-MM-dd || yyyy-MM-ddTHH:mm:ss)
  val dMHms      = P(CharIn('0' to '9').rep(min = 2, max = 2))
  val year       = P(CharIn('0' to '9').rep(min = 4, max = 4))
  val seconds    = P(dMHms ~ ("." ~ CharIn('0' to '9').rep(max = 3)).?)
  val time       = P("T" ~ dMHms ~ ":" ~ dMHms ~ ":" ~ seconds)
  val tz         = P((("-" | "+") ~ dMHms ~ ":" ~ dMHms) | "Z")
  val dateFormat = P(year ~ "-" ~ dMHms ~ "-" ~ dMHms ~ time.? ~ tz.?)
  val date       = dateFormat.!.map(parseDate)

  // A sequence of chars
  val charSeq = P(CharIn('A' to 'Z', 'a' to 'z', '0' to '9', "_-"))

  // A parser for open parens
  val openParen = P("(" ~ space)

  // A parser for look ahead of open parens
  val openParenLah = P(&("("))

  // A parser for closed parens
  val closeParen = P(space ~ ")")

  // A parser for look ahead of closed parens
  val closeParenLah = P(space ~ &(")"))

  // A parser for open brackets
  val openBracket = P("[" ~ space)

  // A parser for close brackets
  val closeBracket = P(space ~ "]")

  // A parser for a parentized block
  def parenBlock[T](p: Parser[T]): Parser[Vector[T]] =
    openParen ~ p.rep(sep = ",").map(_.toVector) ~ closeParen

  // A parser for a quoted block
  def quoted[T](p: Parser[T]): Parser[String] =
    P("\"" ~ (space ~ p ~ space).! ~ "\"")

  // A parser for a block surrounded for one space at least
  def monospaced[T](p: Parser[T]): Parser[T] =
    P(oneSpaceAtLeast ~ p ~ oneSpaceAtLeast)

  // A parser for a list of elements
  def list[T](p: Parser[T]): Parser[List[T]] =
    openBracket ~ (space ~ p ~ space).rep(sep = ",").map(_.toList) ~ closeBracket
}