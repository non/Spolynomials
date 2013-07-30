package SPolynomials

import scala.math.{Ordering => ScalaOrdering} 
import spire.algebra._
import spire.math._
import spire.implicits._
import spire.syntax._


// Univariate Poly Term
case class Term[F: Field](val coeff: F, val index: Int) {

	def eval(x: F): F = coeff * (x ** index)

	def isIndexZero: Boolean = index == 0

	def divideBy(x: F): Term[F] = Term(coeff / x, index)

	def negate: Term[F] = Term(coeff.unary_-, index)

	def der: Term[F] = Term(coeff * index, index - 1)

	def int: Term[F] = Term(coeff / (index + 1), index + 1)

	override def toString = (coeff, index) match {
		case (0, i) => ""
		case (1, 1) => "x"
		case (1, 0) => "1"
		case (c, 1) => s"${c}x"
		case (1, i) => s"x^$i"
		case (c, 0) => s"${c}"
		case (c, i) => s"${c}x^$i"
	}
}

trait TermAdditiveSemigroup[F] extends AdditiveSemigroup[Term[F]] {
	implicit def F: Field[F]
	def plus(x: Term[F], y: Term[F]): Term[F] = 
		Term(x.coeff + y.coeff, y.index)
}

trait TermMultiplicativeSemigroup[F] extends MultiplicativeSemigroup[Term[F]] {
	implicit def F: Field[F]
	def times(x: Term[F], y: Term[F]): Term[F] = 
		Term(x.coeff * y.coeff, x.index + y.index)
}

// Univariate Poly Class
final class Poly[F](val terms: List[Term[F]])
									 (implicit F: Field[F]) {

	implicit object BigEndianPolyOrdering extends ScalaOrdering[Term[F]] {
	  def compare(x:Term[F], y:Term[F]) : Int =
	  	if(x.index < y.index) 1 else if(x.index == y.index) 0 else -1
	}

	lazy val coeffs: List[F] = {
		val indices = terms.sorted.map(_.index)
		val fillerTerms = for {
			i <- 0 to maxOrder;
			if(!indices.contains(i))
		} yield Term(F.zero, i)
		(terms ++ fillerTerms.toList).sorted.map(_.coeff)
	}

	lazy val maxOrder: Int = terms.min.index

	lazy val maxOrderTermCoeff: F = terms.min.coeff

	lazy val maxTerm: Term[F] = terms.min
	
	def apply(x: F): F = terms.map(_.eval(x)).foldLeft(F.zero)(_ + _)

	def isZero: Boolean = maxOrder == 0 && maxOrderTermCoeff == F.zero

	def monic: Poly[F] = new Poly(terms.map(_.divideBy(maxOrderTermCoeff)))
	
	def derivative: Poly[F] = new Poly(terms.filterNot(_.isIndexZero).map(_.der))
	
	def integral: Poly[F] = new Poly(terms.map(_.int))
	
	override def toString = checkString(terms.sorted.mkString(" + "))
	
	private def checkString(s: String) : String = 
		if(s.reverse.take(3) == " + ") checkString(s.dropRight(3)) else s

}

// Companion object for Poly
object Poly {

	implicit def ring[F: Field] = new PolynomialRing[F] {
    val F = Field[F]
  }

	def apply[F: Field](terms: (F, Int)*): Poly[F] =
		new Poly(terms.toList.map({case (c, i) => Term(c, i)}))

}

// Polynomials form a Euclidean Ring
trait PolynomialRing[F] extends EuclideanRing[Poly[F]] {

  implicit def F: Field[F]

  implicit def TAdder[F: Field] = new TermAdditiveSemigroup[F] {
  	val F = Field[F]
  }

  implicit def TMultiplier[F: Field] = new TermMultiplicativeSemigroup[F] {
  	val F = Field[F]
  }

  def zero = new Poly(List(Term(F.zero, 0)))

  def one = new Poly(List(Term(F.one, 0)))

  def plus(x: Poly[F], y: Poly[F]) : Poly[F] =
  	new Poly((x.terms ++ y.terms).groupBy(_.index).values.toList.map {
  		l => l.foldLeft(Term(F.zero, 0))(_ + _)
  	})

  def negate(x: Poly[F]): Poly[F] =
  	new Poly(x.terms.map(_.negate))

  def times(x: Poly[F], y: Poly[F]) : Poly[F] = {
  	val allTerms = x.terms.flatMap(xterm => y.terms.map(_ * xterm))
  	new Poly(allTerms.groupBy(_.index).values.toList.map {
  		l => l.foldLeft(Term(F.zero, 0))(_ + _)
  	})
  }

  // Euclidean Ring functions

  def quotMod(x: Poly[F], y: Poly[F]): (Poly[F], Poly[F]) = {
  	require(!y.isZero, "Can't divide by polynomial of zero!")
		def eval(q: List[F], u: List[F], n: Int): (Poly[F], Poly[F]) = {
			val v0 : F = y.coeffs match {
				case Nil => F.zero
				case v::vs => v
			}
			(u == Nil || n < 0) match {
				case true => (new Poly(makeTermsLE(q)), new Poly(makeTermsBE(u)))
				case false => eval((u.head / v0) :: q, 
														zipSum(u, y.coeffs.map(num => (num * (u.head / v0)).unary_-)).tail,
														n - 1)
			}
		}
		eval(Nil, x.coeffs, x.maxOrder - y.maxOrder)
  }

  def quot(x: Poly[F], y: Poly[F]): Poly[F] = quotMod(x, y)._1
  
  def mod(x: Poly[F], y: Poly[F]) : Poly[F] = quotMod(x, y)._2

  def zipSum(x: List[F], y: List[F]): List[F] = x.zip(y).map { case (a,b) => a + b }

  def makeTermsBE(xs: List[F]): List[Term[F]] = 
  	xs.zip((0 until xs.length).toList.reverse).map({ case (c, i) => Term(c, i) })

  def makeTermsLE(xs: List[F]): List[Term[F]] = 
  	xs.zip((0 until xs.length).toList).map({ case (c, i) => Term(c, i) })

  def gcd(x: Poly[F], y: Poly[F]) : Poly[F] = {
  	require(!x.isZero || !y.isZero, "Can't evaluate gcd for polynomials of zero!")
  	if(y.isZero) x.monic else gcd(y, mod(x, y))
  }

}

import spire.syntax.literals._
Poly(r"1/10" -> 3, r"1/20" -> 2, r"2/1" -> 1)
Poly(r"1/4" -> 3, r"1/10" -> 2, r"3/1" -> 1)

