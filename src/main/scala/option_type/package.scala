/*
  reference:
  http://danielwestheide.com/blog/2012/12/19/the-neophytes-guide-to-scala-part-5-the-option-type.html
*/

package object example_22 {

  val absentGreeting: Option[String] = Option(null)
  val presentGreeting: Option[String] = Option("Hello!")

  def main(args: Array[String]) {
    println(absentGreeting) // None
    println(presentGreeting) // Some(Hello!)
  }
}

case class User(id: Int, firstName: String, lastName: String, age: Int, gender: Option[String])

object UserRepository {
  private val users = Map(1 -> User(1, "John", "Doe", 28, Some("male")),
                          2 -> User(2, "Johanna", "Doe", 30, None))

  def findById(id: Int): Option[User] = users.get(id)
  def findAll = users.values
}

package object example_23 {
  def main(args: Array[String]) {
    val user1 = UserRepository.findById(1)
    if (user1.isDefined) println(user1.get.firstName) // John

    val user2 = UserRepository.findById(2)
    println("Gender: " + user2.get.gender.getOrElse("not specified")) // Gender: not specified

    val gender = user2.get.gender match {
      case Some(value) => value
      case None => "not specified"
    }
    println("Gender: " + gender) // Gender: not specified
  }
}

package object example_24 {
  def main(args: Array[String]) {
    UserRepository.findById(2).foreach(user => println(user.firstName)) // Johanna

    val age = UserRepository.findById(1).map(_.age)
    println(age) // Some(28)

    val gender = UserRepository.findById(1).map(_.gender)
    println(gender) // Some(Some(male)) -> gender is an Option[Option[String]]

    val gender1 = UserRepository.findById(1).flatMap(_.gender)
    println(gender1) // Some(male))

    val gender2 = UserRepository.findById(2).flatMap(_.gender)
    println(gender2) // None
  }
}

package object example_25 {
  def main(args: Array[String]) {
    val names = List(
      List("John", "Johanna"),
      List(),
      List("Doe")
    )

    println(names.map(_.map(_.toUpperCase))) // List(List(JOHN, JOHANNA), List(), List(DOE)

    println(names.flatMap(_.map(_.toUpperCase))) // List(JOHN, JOHANNA, DOE)
  }
}

package object example_26 {
  def main(args: Array[String]) {
    val names = List(Some("John"), Some("Johanna"), None, Some("Doe"))

    println(names.map(_.map(_.toUpperCase))) // List(Some(JOHN), Some(JOHANNA), None, Some(DOE))

    println(names.flatMap(_.map(_.toUpperCase))) // List(JOHN, JOHANNA, DOE)
  }
}

package object example_27 {
  def main(args: Array[String]) {
    val user = UserRepository.findById(1).filter(_.age > 30)
    println(user) // None

    println(UserRepository.findById(1).filter(_.age > 18)) // Some(User(1,John,Doe,28,Some(male)))
  }
}

package object example_28 {
  def main(args: Array[String]) {
    val gender = for {
      user <- UserRepository.findById(1)
      gender <- user.gender
    } yield gender
    println(gender) // Some(male) -> if users is not found then gender is None

    val genders = for {
      user <- UserRepository.findAll
      gender <- user.gender
    } yield gender
    println(genders) // List(male)

    val list = for {
      User(_, _, _, _, Some(gender)) <- UserRepository.findAll
    } yield gender
    println(list) // List(male)
  }
}

package object example_29 {

  case class Resource(content: String)

  val resourceFromConfigDir: Option[Resource] = None

  val resourceFromClasspath: Option[Resource] = Some(Resource("I was found in classpath"))

  val resource = resourceFromConfigDir orElse resourceFromClasspath

  def main(args: Array[String]) {
    println(resource) // Some(Resource(I was found in classpath))
  }
}