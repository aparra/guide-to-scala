import java.net.{MalformedURLException, URL}

import scala.io.Source
import scala.util.control.Exception.catching


/*
  reference:
  http://danielwestheide.com/blog/2013/01/02/the-neophytes-guide-to-scala-part-7-the-either-type.html

  Try is not really an all-out replacement for Either, only for one particular usage of it,
  namely handling exceptions in a functional way. As it stands, Try and Either really complement
  each other, each covering different use cases.
*/

package object example_35 {

  def getContent(url: URL): Either[String, Source] =
    if (url.getHost.contains("google"))
      Left("Requested URL is blocked")
    else
      Right(Source.fromURL(url))

  def main(args: Array[String]) {
    val google = new URL("http://google.com")

    getContent(google) match {
      case Left(msg) => println(msg) // Requested URL is blocked
      case Right(source) => source.getLines.foreach(println)
    }

    /*
      By calling left or right on an Either value, you get a LeftProjection or RightProjection,
      respectively, which are basically left- or right-biased wrappers for the Either. Once you
      have a projection, you can call map on it:
    */
    val content: Either[String, Iterator[String]] = getContent(google).right.map(_.getLines)
    println(content) // Left(Requested URL is blocked)

    val moreContent: Either[Iterator[String], Source] = getContent(google).left.map(Iterator(_))
    println(moreContent) // Left(non-empty iterator)

    val otherContent: Iterator[String] = getContent(new URL("http://google.com")).fold(Iterator(_), _.getLines())
    println(otherContent) // non-empty iterator
  }
}

package object example_36 {

  def getContent(url: URL): Either[String, Source] =
    if (url.getHost.contains("google"))
      Left("Requested URL is blocked")
    else
      Right(Source.fromURL(url))

  def main(args: Array[String]) {
    val (part5, part6) = (new URL("http://t.co/UR1aalX4"), new URL("http://t.co/6wlKwTmu"))
    val content = getContent(part5).right.flatMap(a =>
      getContent(part6).right.map(b =>
        (a.getLines.size + b.getLines.size) / 2
      )
    )
    println(content) // Right(539)
  }
}

package object example_37 {

  def getContent(url: URL): Either[String, Source] =
    if (url.getHost.contains("google"))
      Left("Requested URL is blocked")
    else
      Right(Source.fromURL(url))

  def averageLineCount(url1: URL, url2: URL): Either[String, Int] = for {
    source1 <- getContent(url1).right
    source2 <- getContent(url2).right
  } yield (source1.getLines.size + source2.getLines.size) / 2

  def main(args: Array[String]) {
    val (part1, part2) = (new URL("http://t.co/UR1aalX4"), new URL("http://t.co/6wlKwTmu"))
    println(averageLineCount(part1, part2)) // Right(539)
    println(averageLineCount(part1, new URL("http://google.com"))) // Left(Requested URL is blocked)
  }
}

package object example_38 {

  def getContent(url: URL): Either[String, Source] =
    if (url.getHost.contains("google"))
      Left("Requested URL is blocked")
    else
      Right(Source.fromURL(url))

  /*
    This won’t compile! The reason will become clearer if we examine what this for comprehension
    corresponds to, if you take away the sugar.

    def averageLineCountWontCompile(url1: URL, url2: URL): Either[String, Int] = for {
      source1 <- getContent(url1).right
      source2 <- getContent(url2).right
      lines1 = source1.getLines.size
      lines2 = source2.getLines.size
    } yield (lines1 + lines2) / 2

    It translates to something that is similar to the following, albeit much less readable:

    def averageLineCountDesugaredWontCompile(url1: URL, url2: URL): Either[String, Int] =
      getContent(url1).right.flatMap { source1 =>
        getContent(url2).right.map { source2 =>
          val lines1 = source1.getLines().size
          val lines2 = source2.getLines().size
          (lines1, lines2)
        }.map { case (x, y) => x + y / 2 }
      }

    The problem is that by including a value definition in our for comprehension, a new call to map
    is introduced automatically – on the result of the previous call to map, which has returned an
    Either, not a RightProjection. As you know, Either doesn’t define a map method, making the compiler
    a little bit grumpy.
  */

  def averageLineCount(url1: URL, url2: URL): Either[String, Int] = for {
    source1 <- getContent(url1).right
    source2 <- getContent(url2).right
    lines1 <- Right(source1.getLines().size).right
    lines2 <- Right(source2.getLines().size).right
  } yield (lines1 + lines2) / 2

  def main(args: Array[String]) {
    val (part1, part2) = (new URL("http://t.co/UR1aalX4"), new URL("http://t.co/6wlKwTmu"))
    println(averageLineCount(part1, part2)) // Right(539)
  }
}

package object example_39 {

  /*
    When to use Either:
    -> Error handling: You can use Either for exception handling very much like Try. Either has one
    advantage over Try: you can have more specific error types at compile time, while Try uses Throwable
    all the time. This means that Either can be a good choice for expected errors.
  */

  def handling[E <: Throwable, T](eType: Class[E])(block: => T): Either[E, T] =
    catching(eType).either(block).asInstanceOf[Either[E, T]]

  def parseURL(url: String): Either[MalformedURLException, URL] =
    handling(classOf[MalformedURLException])(new URL(url))

  /*
    You will have other expected error conditions, and not all of them result in third-party code
    throwing an exception you need to handle, as in the example above
  */

  case class Customer(age: Int)
  class Cigarettes
  case class UnderAgeFailure(age: Int, required: Int)

  def buyCigarettes(customer: Customer): Either[UnderAgeFailure, Cigarettes] =
    if (customer.age < 16) Left(UnderAgeFailure(customer.age, 16))
    else Right(new Cigarettes)
}

package object example_40 {

  type Citizen = String
  case class BlackListedResource(url: URL, visitors: Set[Citizen])

  val blacklist = List(
    BlackListedResource(new URL("https://google.com"), Set("John Doe", "Johanna Doe")),
    BlackListedResource(new URL("http://yahoo.com"), Set.empty),
    BlackListedResource(new URL("https://maps.google.com"), Set("John Doe")),
    BlackListedResource(new URL("http://plus.google.com"), Set.empty)
  )

  val checkedBlacklist: List[Either[URL, Set[Citizen]]] =
    blacklist.map(resource =>
    if (resource.visitors.isEmpty) Left(resource.url)
    else Right(resource.visitors))

  val suspiciousResources = checkedBlacklist.flatMap(_.left.toOption)
  val problemCitizens = checkedBlacklist.flatMap(_.right.toOption).flatten.toSet
}