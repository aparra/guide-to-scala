/*
  reference:
  http://danielwestheide.com/blog/2012/12/12/the-neophytes-guide-to-scala-part-4-pattern-matching-anonymous-functions.html
*/

package object example_17 {

  def main(args: Array[String]) {
    val songTitles = List("Pale Blue Eyes", "Marquee Moon", "Walk On the Wild Side")
    print(songTitles.map(_.toLowerCase).mkString(", ")) // pale blue eyes, marquee moon, walk on the wild side
  }
}

package object example_18 {

  /*
    Accessing the fields of the tuple looks pretty ugly.
  */

  def wordsWithoutOutliers(wordFrequencies: Seq[(String, Int)]): Seq[String] =
    wordFrequencies.filter(wf => wf._2 > 3 && wf._2 < 25).map(_._1)

  def main(args: Array[String]) {
    val wordFrequencies = ("habitual", 6) :: ("and", 56) :: ("consuetudinary", 2) :: ("additionally", 27) :: ("homely", 5) :: ("society", 13) :: Nil
    print(wordsWithoutOutliers(wordFrequencies)) // List(habitual, homely, society)
  }
}

package object example_19 {

  /*
    A pattern matching anonymous function is an anonymous function that is defined as a
    block consisting of a sequence of cases, surrounded as usual by curly braces, but
    without a match keyword before the block. You have make sure that for all possible
    inputs, one of your cases matches so that your anonymous function always returns a
    value. Otherwise, you will risk a `MatchError` at runtime.
  */

  def wordsWithoutOutliers(wordFrequencies: Seq[(String, Int)]): Seq[String] =
    wordFrequencies.filter { case (_, frequency) => frequency > 3 && frequency < 25 } map { case (word, _) => word }

  def main(args: Array[String]) {
    val wordFrequencies = ("habitual", 6) :: ("and", 56) :: ("consuetudinary", 2) :: ("additionally", 27) :: ("homely", 5) :: ("society", 13) :: Nil
    print(wordsWithoutOutliers(wordFrequencies)) // List(habitual, homely, society)
  }
}

package object example_20 {

  /*
    scala API: `def collect[B](pf: PartialFunction[A, B])`
  */

  val wordsWithoutOutliers = new PartialFunction[(String, Int), String] {
    override def apply(wordFrequency: (String, Int)): String = wordFrequency match {
      case (word, frequency) if isNotOutlier(frequency) => word
    }

    override def isDefinedAt(wordFrequency: (String, Int)): Boolean = wordFrequency match {
      case (word, frequency) if isNotOutlier(frequency) => true
      case _ => false
    }

    private def isNotOutlier(frequency: Int) = frequency > 3 && frequency < 25
  }

  def main(args: Array[String]) {
    val wordFrequencies = ("habitual", 6) :: ("and", 56) :: ("consuetudinary", 2) :: ("additionally", 27) :: ("homely", 5) :: ("society", 13) :: Nil

    /*
      Will throw a MatchError if you call map
      print(wordFrequencies.map(wordsWithoutOutliers))
      Exception in thread "main" scala.MatchError: (and,56) (of class scala.Tuple2) ...
    */

    print(wordFrequencies.collect(wordsWithoutOutliers)) // List(habitual, homely, society)
  }
}

package object example_21 {

  val wordsWithoutOutliers: PartialFunction[(String, Int), String] = {
    case (word, frequency) if frequency > 3 && frequency < 25 => word
  }

  def main(args: Array[String]) {
    val wordFrequencies = ("habitual", 6) :: ("and", 56) :: ("consuetudinary", 2) :: ("additionally", 27) :: ("homely", 5) :: ("society", 13) :: Nil
    print(wordFrequencies.collect(wordsWithoutOutliers)) // List(habitual, homely, society)
  }
}
