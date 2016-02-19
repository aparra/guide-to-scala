import scala.collection.immutable.Stream.#::

package object example_5 {

  def main(args: Array[String]) {
    val xs = 58 #:: 43 #:: 93 #:: Stream.empty
    val result = xs match {
      case first #:: second #:: _ => first - second
      case _ => -1
    }
    println(result) // 15
  }

  /*
    code from scala/collection/immutable/Stream.scala
    An extractor that allows to pattern match streams with `#::`.
    object #:: {
      def unapply[A](xs: Stream[A]): Option[(A, Stream[A])] =
        if (xs.isEmpty) None
        else Some((xs.head, xs.tail))
    }
  */
}

package object example_6 {

  def main(args: Array[String]) {
    val xs = 58 #:: 43 #:: 93 #:: Stream.empty
    val result = xs match {
      case #::(first, #::(second, _)) => first - second
      case _ => -1
    }
    println(result) // 15
  }
}