/*
  reference:
  http://danielwestheide.com/blog/2012/12/05/the-neophytes-guide-to-scala-part-3-patterns-everywhere.html
*/

package object example_11 {

  case class Player(name: String, score: Int)

  def message(player: Player) = player match {
    case Player(_, score) if score > 100000 => "Get a job, dude!"
    case Player(name, _) => s"Hey $name, nice to see you again!"
  }

  def main(args: Array[String]) {
    println(message(Player("Ligia", 100001))) // Get a job, dude!

    println(message(Player("Anderson", 10)))  // Hey Anderson, nice to see you again!
  }
}

package object example_12 {

  case class Player(name: String, score: Int)

  def currentPlayer: Player = Player("Anderson", 10)

  def doSomethingWithTheName(name: String) = ???

  def main(args: Array[String]) {
    val player = currentPlayer
    doSomethingWithTheName(player.name)

    /*
      If you know Python, you are probably familiar with a feature called `sequence unpacking`.
      The fact that you can use any pattern in the left side of a value definition or variable
      definition lets you write your Scala code in a similar style. We could change our above
      code and destructure the given current player while assigning it to the left side
    */
    val Player(name, _) = currentPlayer
    doSomethingWithTheName(name)
  }
}

package object example_13 {

  def scores: List[Int] = List()

  def main(args: Array[String]) {
    /*
      You can do this with any pattern, but generally, it is a good idea to make sure that your
      pattern always matches. Otherwise, you will be the witness of an exception at runtime.
    */

    val best :: rest = scores
    println(s"The score of our champion is $best")
    // Exception in thread "main" scala.MatchError: List()
  }
}

package object example_14 {

  def gameResult: (String, Int) = ("Anderson", 3)

  def main(args: Array[String]) {
    val result = gameResult
    println(result._1 + ": " + result._2) // Anderson: 3

    val (name, score) = gameResult
    println(s"$name: $score") // Anderson: 3
  }
}

package object example_15 {

  def gameResults: Seq[(String, Int)] =
    ("Anderson", 3) :: ("Ligia", 9) :: ("Donald Duck", 10) :: Nil

  def hallOfFame = for {
    (name, score) <- gameResults
    if score > 8
  } yield name

  def main(args: Array[String]) {
    println(hallOfFame.mkString(", ")) // Ligia, Donald Duck
  }
}

package object example_16 {

  def main(args: Array[String]) {
    val lists = List(1, 2, 3) :: List.empty :: List(4, 5) :: List.empty

    val sizes = for {
      list @ head :: _ <- lists
    } yield list.size

    println(sizes) // List(3, 2)
  }
}