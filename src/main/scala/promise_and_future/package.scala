import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

/*
  reference:
  http://danielwestheide.com/blog/2013/01/16/the-neophytes-guide-to-scala-part-9-promises-and-futures-in-practice.html
*/

package object example_44 {

  /*
    A Promise instance is always linked to exactly one instance of Future. If you call the apply method
    of Future again in the REPL (read-eval-print loop), you will indeed notice that the Future returned
    is a Promise.
  */

  def main(args: Array[String]) {
    import concurrent.ExecutionContext.Implicits.global

    val f: Future[String] = Future {
      "Hello World!"
    }

    // The object you get back is a DefaultPromise, which implements both Future and Promise
    println(f) // scala.concurrent.impl.Promise$DefaultPromise
  }
}

package object example_45 {

  case class TaxCut(reduction: Int)

  // either give the type as a type parameter to the factory method:
  val taxcut = Promise[TaxCut]()
  // or give the compiler a hint by specifying the type of your val:
  val taxcut2: Promise[TaxCut] = Promise()

  def main(args: Array[String]) {
    val taxcutF: Future[TaxCut] = taxcut.future
    println(taxcutF) // scala.concurrent.impl.Promise$DefaultPromise
    println(taxcut) // scala.concurrent.impl.Promise$DefaultPromise

    /*
      The returned Future might not be the same object as the Promise, but calling the future method of a
      Promise multiple times will definitely always return the same object to make sure the one-to-one
      relationship between a Promise and its Future is preserved.
    */

    /*
      To complete a Promise with a success, you call its success method, passing it the value that the
      Future associated with it is supposed to have:
    */
    taxcut.success(TaxCut(20))
  }
}

package object example_46 {

  import concurrent.ExecutionContext.Implicits.global

  case class TaxCut(reduction: Int)

  object Government {
    def redeemCampaignPledge(): Future[TaxCut] = {
      val promise = Promise[TaxCut]()
      Future {
        println("Starting the new legislative period")
        promise.success(TaxCut(20))
        println("We reduced the taxes! You must reelect us!")
      }
      promise.future
    }
  }

  def main(args: Array[String]) {
    val taxcutF: Future[TaxCut] = Government.redeemCampaignPledge()
    println("Now that they're elected, let's see if they remember their promises...")
    taxcutF.onComplete {
      case Success(TaxCut(reduction)) =>
        println(s"A miracle! They really cut our taxes by $reduction percentage points!")
      case Failure(e) =>
        println(s"They broke their promises! Again! Because of a ${e.getMessage}")
    }
    Thread.sleep(1000)
  }
}

package object example_47 {

  import concurrent.ExecutionContext.Implicits.global

  case class LameExcuse(msg: String) extends Exception(msg)

  case class TaxCut(reduction: Int)

  object Government {
    def redeemCampaignPledge(): Future[TaxCut] = {
      val promise = Promise[TaxCut]()
      Future {
        println("Starting the new legislative period")
        promise.failure(LameExcuse("global economy crisis"))
        println("We didn't fulfill our promises, but surely they'll understand.")
      }
      promise.future
    }
  }

  def main(args: Array[String]) {
    val taxcutF: Future[TaxCut] = Government.redeemCampaignPledge()
    println("Now that they're elected, let's see if they remember their promises...")
    taxcutF.onComplete {
      case Success(TaxCut(reduction)) =>
        println(s"A miracle! They really cut our taxes by $reduction percentage points!")
      case Failure(e) =>
        println(s"They broke their promises! Again! Because of a ${e.getMessage}")
    }
    Thread.sleep(1000)
  }
}

package object example_48 {

  /*
    Sometimes, there is no NIO-based library available. For instance, most database drivers you’ll
    find in the Java world nowadays are using blocking IO.
  */
  def queryDb(query: String) = ???

  /*
    If you made a query to your database with such a driver in order to respond to a HTTP request,
    that call would be made on a web server thread. To avoid that, place all the code talking to
    the database inside a future block.
  */
  Future {
    queryDb("select * from ...") // get back a Future[ResultSet] or something similar:
  }

  /*
    You can create an ExecutionContext from a Java ExecutorService, which means you will be able to
    tune the thread pool for executing your database calls asynchronously independently from the
    rest of your application:
  */
  val executorService = Executors.newFixedThreadPool(4)
  val executionContext = ExecutionContext.fromExecutorService(executorService)

  def longRunningComputation(data: Any, moreData: Any) { }

  /*
    Long-running computations: depending on the nature of your application, it will occasionally have
    to call long-running tasks that don’t involve any IO at all, which means they are CPU-bound. These,
    too, should not be executed by a web server thread. Hence, you should turn them into Futures, too:
  */
  Future {
    longRunningComputation(data = ???, moreData = ???)
  }
}