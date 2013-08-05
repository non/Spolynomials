package spolynomials

import spire.algebra._
import spire.math._
import spire.implicits._
import spire.syntax._

// Univariate polynomial term
case class Term[C, E](val coeff: C, val exp: E)
										 (implicit cord: Order[C], 
										 					 eord: Order[E],
										 					 cring: Ring[C],
										 					 ering: Ring[E],
										 					 conve: ConvertableFrom[E]) {
	require(eord.gteqv(exp, ering.zero), 
						"Polynomials terms must have a positive or zeroth order exponent")

	def eval(x: C): C = cring.times(coeff, cring.pow(x, conve.toInt(exp)))

	def isIndexZero: Boolean = eord.eqv(exp, ering.zero)

	def isZero: Boolean = cord.eqv(coeff, cring.zero)

	def divideBy(x: C)(implicit cfield: Field[C]): Term[C, E] = 
		Term(cfield.div(coeff, x), exp)

	def der: Term[C, E] = 
		Term(cring.times(coeff, cring.fromInt(conve.toInt(exp))), 
					ering.minus(exp, ering.one))

	def int(implicit cfield: Field[C]): Term[C, E] = 
		Term(cfield.div(coeff, cring.fromInt(conve.toInt(ering.plus(exp, ering.one)))), 
					ering.plus(exp, ering.one))

	override def toString = {
		val pm = cord.compare(coeff, cring.zero)
		(coeff, exp) match {
			case (0, i) => ""
			case (1, 0) => if(pm >= 0) s" + 1" else s" - 1"
			case (-1, 1) => s" - x"
			case (1, 1) => s" + x"
			case (c, 1) => if(pm >= 0) s" + ${c}x" else s" - ${c.unary_-}x"
			case (1, i) => if(pm >= 0) s" + x^$i" else s" - x^$i"
			case (c, 0) => if(pm >= 0) s" + ${c}" else s" - ${c.unary_-}"
			case (c, i) => if(pm >= 0) s" + ${c}x^$i" else s" - ${c.unary_-}x^$i"
		}
	}

}

// Univariate polynomial terms form a ring
trait TermRing[C, E] extends Ring[Term[C, E]] {

	implicit val cord: Order[C]
	implicit val eord: Order[E]
	implicit val cring: Ring[C]
	implicit val ering: Ring[E]
	implicit val conve: ConvertableFrom[E]

	def negate(t: Term[C, E])(implicit cring: Ring[C]): Term[C, E] =
		Term(cring.negate(t.coeff), t.exp)

	def zero(implicit cring: Ring[C], ering: Ring[E]): Term[C, E] =
		Term(cring.zero, ering.zero)

	def one(implicit cring: Ring[C], ering: Ring[E]): Term[C, E] = 
		Term(cring.one, ering.zero)

	def plus(x: Term[C, E], y: Term[C, E])
					(implicit cring: Ring[C]): Term[C, E] = 
		Term(cring.plus(x.coeff, y.coeff), y.exp)

	def times(x: Term[C, E], y: Term[C, E])
					 (implicit cring: Ring[C], ering: Ring[E]): Term[C, E] = 
		Term(cring.times(x.coeff, y.coeff), ering.plus(x.exp, y.exp))

}