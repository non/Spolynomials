package spolynomials

import spire.algebra._
import spire.math._
import spire.implicits._
import spire.syntax._

// Univariate polynomials form a Euclidean Ring
trait PolynomialRing[F] extends EuclideanRing[Poly[F]] {

  implicit def F: Field[F]

  implicit def TR[F: Field] = new TermRing[F] {
  	val F = Field[F]
  }

  def zero = new Poly(Nil)

  def one = new Poly(List(Term(F.one, 0)))

  def plus(x: Poly[F], y: Poly[F]) : Poly[F] =
  	new Poly((x.terms ++ y.terms).groupBy(_.index).values.toList.map {
  		l => l.foldLeft(Term(F.zero, 0))(_ + _)
  	})

  def negate(x: Poly[F]): Poly[F] =
  	new Poly(x.terms.map(_.unary_-))

  def times(x: Poly[F], y: Poly[F]) : Poly[F] = {
  	val allTerms = x.terms.flatMap(xterm => y.terms.map(_ * xterm))
  	new Poly(allTerms.groupBy(_.index).values.toList.map {
  		l => l.foldLeft(Term(F.zero, 0))(_ + _)
  	})
  }

  def quotMod(x: Poly[F], y: Poly[F]): (Poly[F], Poly[F]) = {
  	require(!y.isZero, "Can't divide by polynomial of zero!")
		def eval(q: List[F], u: List[F], n: Int): (Poly[F], Poly[F]) = {
			val v0 : F = y.coeffs match {
				case Nil => F.zero
				case v::vs => v
			}
			(u == Nil || n < 0) match {
				case true => (new Poly(makeTermsLE(q)), new Poly(makeTermsBE(u)))
				case false => eval(
          (u.head / v0) :: q, 
					zipSum(u, y.coeffs.map(z => (z * (u.head / v0)).unary_-)).tail,
					n - 1
        )
			}
		}
		eval(Nil, x.coeffs, x.maxOrder - y.maxOrder)
  }

  def quot(x: Poly[F], y: Poly[F]): Poly[F] = quotMod(x, y)._1
  
  def mod(x: Poly[F], y: Poly[F]) : Poly[F] = quotMod(x, y)._2

  def zipSum(x: List[F], y: List[F]): List[F] = 
  	x.zip(y).map { case (a,b) => a + b }

  def makeTermsBE(xs: List[F]): List[Term[F]] = 
  	xs.zip((0 until xs.length).toList.reverse).map({ 
  		case (c, i) => Term(c, i) })

  def makeTermsLE(xs: List[F]): List[Term[F]] = 
  	xs.zip((0 until xs.length).toList).map({ 
  		case (c, i) => Term(c, i) })

  def gcd(x: Poly[F], y: Poly[F]) : Poly[F] = 
  	if(y.isZero && x.isZero) zero else if(y.isZero) x.monic 
      else gcd(y, mod(x, y))

}