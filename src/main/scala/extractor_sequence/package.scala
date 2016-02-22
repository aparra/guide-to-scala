/*
  reference:
  http://danielwestheide.com/blog/2012/11/28/the-neophytes-guide-to-scala-part-2-extracting-sequences.html
*/

package object example_7 {

  def main(args: Array[String]) {
    val xs = 3 :: 6 :: 12 :: Nil
    val value = xs match {
      case List(a, b) => a * b
      case List(a, b, c) => a + b + c
      case _ => 0
    }
    println(value) // 21
  }
}

package object example_8 {

  def main(args: Array[String]) {
    val xs = 3 :: 6 :: 12 :: Nil
    val value = xs match {
      case List(a, b, _*) => a * b
      case _ => 0
    }
    println(value) // 18
  }
}

package object example_9 {

  /* It is a possible method signature: `def unapplySeq(object: S): Option[Seq[T]]` */

  object GivenNames {
    def unapplySeq(name: String): Option[Seq[String]] = {
      val names = name.trim.split(" ")
      if (names.forall(_.isEmpty)) None else Some(names)
    }
  }

  def greetWithFirstName(name: String) = name match {
    case GivenNames(firstName, _*) => s"Good morning, $firstName!"
    case _ => "Welcome! Please make sure to fill in your name!"
  }

  def main(args: Array[String]) {
    println(greetWithFirstName("Anderson"))
    // Good morning, Anderson!

    println(greetWithFirstName("Anderson Parra de Paula"))
    // Good morning, Anderson!
  }
}

package object example_10 {

  /* It is another possible method signature: `def unapplySeq(object: S): Option[(T1, ..., Tn-1, Seq[T])]` */

  object Names {
    def unapplySeq(name: String): Option[(String, String, Array[String])] = {
      val names = name.trim.split(" ")
      if (names.size < 2) None
      else Some((names.last, names.head, names.drop(1).dropRight(1)))
    }
  }

  def greet(fullName: String) = fullName match {
    case Names(lastName, firstName, _*) => s"Good morning, $firstName $lastName!"
    case _ => "Welcome! Please make sure to fill in your name!"
  }

  def main(args: Array[String]) {
    println(greet("Anderson"))
    // Welcome! Please make sure to fill in your name!

    println(greet("Anderson Parra"))
    // Good morning, Anderson Parra!

    println(greet("Anderson Parra de Paula"))
    // Good morning, Anderson Paula!
  }
}