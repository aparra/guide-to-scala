import java.io.{FileNotFoundException, InputStream}
import java.net.{MalformedURLException, URL}

import scala.io.Source
import scala.util.{Failure, Success, Try}

/*
  reference:
  http://danielwestheide.com/blog/2012/12/26/the-neophytes-guide-to-scala-part-6-error-handling-with-try.html

  Idiomatic error handling in Scala is quite different from the paradigm known from languages
  like Java or Ruby. The Try type allows you to encapsulate computations that result in errors
  in a container and to chain operations on the computed values in a very elegant way. You can
  transfer what you know from working with collections and with Option values to how you deal
  with code that may result in errors – all in a uniform way.
*/

package object example_30 {

  case class Customer(age: Int)
  class Cigarettes
  case class UnderAgeException(message: String) extends Exception(message)

  def buyCigarettes(customer: Customer): Cigarettes =
    if (customer.age < 16)
      throw UnderAgeException(s"Customer must be older than 16 but was ${customer.age}")
    else new Cigarettes

  def main(args: Array[String]) {
    val youngCustomer = Customer(age = 15)
    try {
      buyCigarettes(youngCustomer)
    } catch {
      case UnderAgeException(msg) => println(msg)
    }
  }
}

package object example_31 {

  def parseURL(url: String): Try[URL] = Try(new URL(url))

  def main(args: Array[String]) {
    val url = parseURL("invalid.url") getOrElse new URL("http://duckduckgo.com")
    println(url) // http://duckduckgo.com

    println(parseURL("http://fake.com").map(_.getProtocol)) // Success(http)
    println(parseURL("garbage").map(_.getProtocol)) // Failure(java.net.MalformedURLException: no protocol: garbage)
  }
}

package object example_32 {

  def parseURL(url: String): Try[URL] = Try(new URL(url))

  def inputStreamFromURL(url: String): Try[Try[Try[InputStream]]] = parseURL(url).map { url =>
    Try(url.openConnection).map(conn => Try(conn.getInputStream))
  }

  /*
    We can basically create a pipeline of operations that require the values carried over in Success
    instances by chaining an arbitrary number of flatMap calls. Any exceptions that happen along the
    way are wrapped in a Failure, which means that the end result of the chain of operations is a
    Failure, too.
  */

  def improvedInputStreamFromURL(url: String): Try[InputStream] = parseURL(url).flatMap { url =>
    Try(url.openConnection).flatMap(conn => Try(conn.getInputStream))
  }
}

package object example_33 {

  def parseURL(url: String): Try[URL] = Try(new URL(url))
  def parseHttpURL(url: String) = parseURL(url).filter(_.getProtocol == "http")

  def main(args: Array[String]) {
    println(parseHttpURL("http://fake.com")) // Success(http://fake.com)
    println(parseHttpURL("ftp://fake.com")) // Failure(java.util.NoSuchElementException: Predicate does not hold for ftp://fake.com)
  }
}

package object example_34 {

  def parseURL(url: String): Try[URL] = Try(new URL(url))

  /*
    The support for flatMap, map and filter means that you can also use for comprehensions in order
    to chain operations on Try instances. More expressive pipeline!
  */

  def getURLContent(url: String): Try[Iterator[String]] = for {
    url <- parseURL(url)
    conn <- Try(url.openConnection)
    is <- Try(conn.getInputStream)
    source = Source.fromInputStream(is)
  } yield source.getLines

  def main(args: Array[String]) {
    getURLContent("http://fake.com") match {
      case Success(lines) => lines foreach println
      case Failure(e) => println(s"Problem rendering URL content: ${e.getMessage}")
    }

    val content = getURLContent("garbage") recover {
      case e: FileNotFoundException => Iterator("Requested page does not exist")
      case e: MalformedURLException => Iterator("Please make sure to enter a valid URL")
      case _ => Iterator("An unexpected error has occurred. We are so sorry!")
    }
  }
}
