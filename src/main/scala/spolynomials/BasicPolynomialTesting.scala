package spolynomials

import scala.reflect._
import spire.algebra._
import spire.math._
import spire.implicits._
import spire.syntax._

object BasicPolynomialTesting extends App {

	import spire.syntax.literals._

	val a: Poly[Rational, Int] = new Poly(Array(Term(r"1/100", 1), Term(r"2/1", 3), Term(r"-17/19", 2)))
	val b: Poly[Rational, Int] = new Poly(Array(Term(r"1/2", 1), Term(r"-3/2", 3), Term(r"1/4", 2)))

	val p1 = new Poly(Array(Term(r"2/1", 2), Term(r"1/1", 1)))
	val p2 = new Poly(Array(Term(r"1/3", 2), Term(r"2/1", 1)))

	println(a.show)
	println(b.show)

	val d = p1 % p2
	val e = p1 /~ p2

	println(d.show)
	println(e.show)

	println(a(r"2/1"))
	println(a.derivative.show)
	println(a.monic.show)

}