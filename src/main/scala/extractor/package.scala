/*
  reference:
  http://danielwestheide.com/blog/2012/11/21/the-neophytes-guide-to-scala-part-1-extractors.html
*/

import java.net.URL

package object example_1 {

  case class User(firstName: String, lastName: String, score: Int)

  def advance(xs: List[User]) = xs match {
    case User(_, _, score1) :: User(_, _, score2) :: _ => score1 - score2
    case _ => 0
  }
}

package object example_2 {

  trait User {
    def name: String
  }

  class FreeUser(val name: String) extends User
  class PremiumUser(val name: String) extends User

  object FreeUser {
    def unapply(user: FreeUser): Option[String] = Some(user.name)
  }
  object PremiumUser{
    def unapply(user: PremiumUser): Option[String] = Some(user.name)
  }

  def welcomeMessageTo(user: User): String = user match {
    case FreeUser(name) => s"Hello $name"
    case PremiumUser(name) => s"Welcome back, dear $name"
  }

  def main(args: Array[String]) {
    val name = FreeUser.unapply(new FreeUser("Anderson"))
    println(name) // Some(Anderson)

    val message = welcomeMessageTo(new PremiumUser("Ligia"))
    println(message) // Welcome back, dear Ligia
  }

  /*
    class FakeUser(val name: String) extends User

    def welcomeMessageTo(user: User): String = user match {
      case FakeUser(name) => s"Fake User $name"
      case FreeUser(name) => s"Hello $name"
      case PremiumUser(name) => s"Welcome back, dear $name"
    }

    FakeUser is not a Case Class and it did not implemented unapply method, then is not possible use FakeUser as
    a pattern matching case.

    The unapply method should looks like this: `def unapply(object: S): Option[T]`
  */
}

package object example_3 {

  trait User {
    def name: String
    def score: Int
  }

  class FreeUser(val name: String, val score: Int, val upgradeProbability: Double) extends User
  class PremiumUser(val name: String, val score: Int) extends User

  object FreeUser {
    def unapply(user: FreeUser): Option[(String, Int, Double)] = Some(user.name, user.score, user.upgradeProbability)
  }
  object PremiumUser {
    def unapply(user: PremiumUser): Option[(String, Int)] = Some(user.name, user.score)
  }

  def welcomeMessageTo(user: User): String = user match {
    case FreeUser(name, _, p) if p > 0.75 => s"$name, what can we do for you today?"
    case FreeUser(name, _, _) => s"Hello $name"
    case PremiumUser(name) => s"Welcome back, dear $name"
  }

  def main(args: Array[String]) {
    println(welcomeMessageTo(new FreeUser(name = "Anderson", score = 100, upgradeProbability = 0.76)))
    // Anderson, what can we do for you today?

    println(welcomeMessageTo(new FreeUser(name = "Fake", score = 100, upgradeProbability = 0.75)))
    // Hello Fake

    println(welcomeMessageTo(new PremiumUser(name = "Ligia", score = 100)))
    // Welcome back, dear Ligia
  }
}

package object example_4 {

  trait User {
    def name: String
    def score: Int
  }

  class FreeUser(val name: String, val score: Int, val upgradeProbability: Double) extends User
  class PremiumUser(val name: String, val score: Int) extends User

  object FreeUser {
    def unapply(user: FreeUser): Option[(String, Int, Double)] = Some(user.name, user.score, user.upgradeProbability)
  }
  object PremiumUser {
    def unapply(user: PremiumUser): Option[(String, Int)] = Some(user.name, user.score)
  }
  object premiumCandidate {
    def unapply(user: FreeUser): Boolean = user.upgradeProbability > 0.75
  }

  def initiateSpamProgram(freeUser: FreeUser) = "program to prospect more premium users"
  def sendRegularNewsLetter(user: User) = "premium users are never to be spammed"

  def notify(user: User): String = user match {
    case freeUser @ premiumCandidate() => initiateSpamProgram(freeUser)
    case _ => sendRegularNewsLetter(user)
  }

  def main(args: Array[String]) {
    val freeUser = new FreeUser(name = "Anderson", score = 100, upgradeProbability = 0.76)
    println(notify(freeUser))
    // program to prospect more premium users

    val premiumUser = new PremiumUser(name = "Ligia", score = 100)
    println(notify(premiumUser))
    // premium users are never to be spammed
  }
}

package object exercise_1 {

  object HttpUrl {
    def unapply(url: URL): Option[String] =
      if (url.getProtocol.startsWith("http")) Some(url.toExternalForm)
      else None
  }

  def main(args: Array[String]) {
    val url = new URL("http://danielwestheide.com") match {
      case HttpUrl(value) => s"website: $value"
      case _ => "???"
    }
    println(url) // website: http://danielwestheide.com
  }
}