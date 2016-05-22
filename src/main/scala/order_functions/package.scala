/*
  reference:
  http://danielwestheide.com/blog/2013/01/23/the-neophytes-guide-to-scala-part-10-staying-dry-with-higher-order-functions.html
*/

package object example_49 {

  case class Email(
    subject: String,
    text: String,
    from: String,
    to: String
  )

  type EmailFilter = Email => Boolean

  def newMailsForUser(emails: Seq[Email])(f: EmailFilter) =
    emails.filter(f)

  val sentByOneOf: Set[String] => EmailFilter =
    senders => email => senders.contains(email.from)

  val notSentByAnyOf: Set[String] => EmailFilter =
    senders => email => !senders.contains(email.from)

  val minimumLength: Int => EmailFilter = n => email => email.text.length >= n

  val maximumLength: Int => EmailFilter = n => email => email.text.length <= n

  def main(args: Array[String]) {
    val emailFilter = notSentByAnyOf(Set("johndoe@example.com"))

    val email = Email(
      subject = "It's me again, your stalker friend!",
      text = "Hello my friend! How are you?",
      from = "johndoe@example.com",
      to = "me@example.com")

    val filteredEmails = newMailsForUser(Seq(email))(emailFilter)
    println(filteredEmails) // List() - returns an empty list
  }
}

package object example_50 {

  case class Email(
    subject: String,
    text: String,
    from: String,
    to: String
  )

  type EmailFilter = Email => Boolean

  type SizeChecker = Int => Boolean

  def newMailsForUser(emails: Seq[Email])(f: EmailFilter) =
    emails.filter(f)

  val sizeConstraint: SizeChecker => EmailFilter =
    f => email => f(email.text.length)

  val minimumLength: Int => EmailFilter = n => sizeConstraint(_ >= n)

  val maximumLength: Int => EmailFilter = n => sizeConstraint(_ <= n)

}

package object example_51 {

  case class Email(
    subject: String,
    text: String,
    from: String,
    to: String
  )

  type EmailFilter = Email => Boolean

  type SizeChecker = Int => Boolean

  def complement[A](predicate: A => Boolean) = (a: A) => !predicate(a)

  def newMailsForUser(emails: Seq[Email])(f: EmailFilter) =
    emails.filter(f)

  val sentByOneOf: Set[String] => EmailFilter =
    senders => email => senders.contains(email.from)

  /*
    Scala functions provide two composing functions that will help us here:
    Given two functions f and g, f.compose(g) returns a new function that,
    when called, will first call g and then apply f on the result of it.
    Similarly, f.andThen(g) returns a new function that, when called, will
    apply g to the result of f.
  */

  val notSentByAnyOf = sentByOneOf andThen(complement(_))
}

package object example_52 {

  case class Email(
    subject: String,
    text: String,
    from: String,
    to: String
  )

  type EmailFilter = Email => Boolean

  type SizeChecker = Int => Boolean

  def newMailsForUser(emails: Seq[Email])(f: EmailFilter) =
    emails.filter(f)

  /*
    Composing predicates
    Another problem with our email filters is that we can currently only pass
    a single EmailFilter to our newMailsForUser function. Certainly, our users
    want to configure multiple criteria. We need a way to create a composite
    predicate that returns true if either any, none or all of the predicates
    it consists of return true.
  */

  def complement[A](predicate: A => Boolean) = (a: A) => !predicate(a)

  def any[A](predicates: (A => Boolean)*): A => Boolean =
    a => predicates.exists(predicate => predicate(a))

  def none[A](predicates: (A => Boolean)*): A => Boolean =
    complement(any(predicates: _*))

  def every[A](predicates: (A => Boolean)*): A => Boolean =
    none(predicates.view.map(complement(_)): _*)

  /*
    The any function returns a new predicate that, when called with an input a,
    checks if at least one of its predicates holds true for the value a. Our none
    function simply returns the complement of the predicate returned by any – if at
    least one predicate holds true, the condition for none is not satisfied.
    Finally, our every function works by checking that none of the complements to
    the predicates passed to it holds true.
  */

  val sentByOneOf: Set[String] => EmailFilter =
    senders => email => senders.contains(email.from)

  val notSentByAnyOf = sentByOneOf andThen(complement(_))

  val sizeConstraint: SizeChecker => EmailFilter =
    f => email => f(email.text.length)

  val minimumLength: Int => EmailFilter = n => sizeConstraint(_ >= n)

  val maximumLength: Int => EmailFilter = n => sizeConstraint(_ <= n)


  def main(args: Array[String]) {
    val emailFilter = every(
      notSentByAnyOf(Set("johndoe@example.com")),
      minimumLength(100),
      maximumLength(10000)
    )

    val email = Email(
      subject = "It's me again, your stalker friend!",
      text = "Hello my friend! How are you?",
      from = "johndoe@example.com",
      to = "me@example.com")

    newMailsForUser(Seq(email))(emailFilter)
  }
}

package object example_53 {

  case class Email(
    subject: String,
    text: String,
    from: String,
    to: String
  )

  val addMissingSubject = (email: Email) =>
    if (email.subject.isEmpty) email.copy(subject = "No Subject")
    else email

  val checkSpelling = (email: Email) =>
    email.copy(text = email.text.replaceAll("your", "you're"))

  val removeInappropriateLanguage = (email: Email)  =>
    email.copy(text = email.text.replaceAll("dynamic typing", "**CENSORED**"))

  val addAdsToFooter = (email: Email) =>
    email.copy(text = email.text + "\nThis mail sent via Super Awesome Free Mail")

  /*
    Now, depending on the weather and the mood of our boss, we can configure our
    pipeline as required, either by multiple andThen calls, or, having the same
    effect, by using the chain method defined on the Function companion object:
  */

  val pipeline = Function.chain(Seq(
    addMissingSubject,
    checkSpelling,
    removeInappropriateLanguage,
    addAdsToFooter
  ))
}

/*
  Lifting partial functions
  Also, sometimes a PartialFunction is not what you need. If you think about it,
  another way to represent the fact that a function is not defined for all input
  values is to have a standard function whose return type is an Option[A] – if the
  function is not defined for an input value, it will return None, otherwise a
  Some[A].

  If that’s what you need in a certain context, given a PartialFunction named pf,
  you can call pf.lift to get the normal function returning an Option. If you
  have one of the latter and require a partial function, call Function.unlift(f).
*/
