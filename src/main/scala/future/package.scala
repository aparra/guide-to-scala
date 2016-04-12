import future._

import scala.concurrent.Future
import scala.util.{Failure, Random, Success, Try}

/*
  reference:
  http://danielwestheide.com/blog/2013/01/09/the-neophytes-guide-to-scala-part-8-welcome-to-the-future.html

  "I heard you like callbacks, so I put a callback in your callback!"
  "I know Futures, and they are completely useless!"
*/

package object future {

  type CoffeeBeans = String
  type GroundCoffee = String
  case class Water(temperature: Int)
  type Milk = String
  type FrothedMilk = String
  type Espresso = String
  type Cappuccino = String

  case class GrindingException(msg: String) extends Exception
  case class FrothingException(msg: String) extends Exception
  case class WaterBoilingException(msg: String) extends Exception
  case class BrewingException(msg: String) extends Exception

}

package object example_41 {

  def grind(beans: CoffeeBeans): GroundCoffee = s"ground coffee $beans"
  def heatWater(water: Water): Water = water.copy(temperature = 85)
  def frothMilk(milk: Milk): FrothedMilk = s"frothed $milk"
  def brew(coffee: GroundCoffee, heatedWater: Water): Espresso = "espresso"
  def combine(espresso: Espresso, frothedMilk: FrothedMilk): Cappuccino = "cappuccino"

  /*
    You get a very readable step-by-step instruction of what to do. Moreover, you will likely not get confused
    while preparing the cappuccino this way, since you are avoiding context switches.

    On the downside, preparing your cappuccino in such a step-by-step manner means that your brain and body
    are on wait during large parts of the whole process. While waiting for the ground coffee, you are effectively
    blocked. Only when that’s finished, you’re able to start heating some water, and so on.
  */

  def prepareCappuccino(): Try[Cappuccino] = for {
    ground <- Try(grind("arabica beans"))
    water <- Try(heatWater(Water(25)))
    espresso <- Try(brew(ground, water))
    foam <- Try(frothMilk("milk"))
  } yield combine(espresso, foam)

  def main(args: Array[String]) {
    val cappuccino = prepareCappuccino()
    println(cappuccino) // Success(cappuccino)
  }
}

package object example_42 {

  implicit val executionContext =  scala.concurrent.ExecutionContext.Implicits.global

  def grind(beans: CoffeeBeans): Future[GroundCoffee] = Future {
    println("start grinding...")
    Thread.sleep(Random.nextInt(2000))
    if (beans == "baked beans") throw GrindingException("are you joking?")
    println("finished grinding...")
    s"ground coffee of $beans"
  }

  def heatWater(water: Water): Future[Water] = Future {
    println("heating the water now")
    Thread.sleep(Random.nextInt(2000))
    println("hot, it's hot!")
    water.copy(temperature = 85)
  }

  def frothMilk(milk: Milk): Future[FrothedMilk] = Future {
    println("milk frothing system engaged!")
    Thread.sleep(Random.nextInt(2000))
    println("shutting down milk frothing system")
    s"frothed $milk"
  }

  def brew(coffee: GroundCoffee, heatedWater: Water): Future[Espresso] = Future {
    println("happy brewing :)")
    Thread.sleep(Random.nextInt(2000))
    println("it's brewed!")
    "espresso"
  }

  def temperatureOkay(water: Water): Future[Boolean] = Future {
    println("we're in the future!")
    (80 to 85).contains(water.temperature)
  }

  def main(args: Array[String]) {
    grind("arabica beans") onSuccess { case ground =>
      println("okay, got my ground coffee")
    }

    grind("backed beans") onComplete {
      case Success(ground) => println(s"got my $ground")
      case Failure(ex) => println("this grinder needs a replacement, seriously!")
    }

    val nestedFuture = heatWater(Water(25)).map {
      water => temperatureOkay(water)
    }
    println(nestedFuture) // scala.concurrent.impl.Promise$DefaultPromise => Future[Future[Boolean]]

    val flatFuture = heatWater(Water(25)).flatMap {
      water => temperatureOkay(water)
    }
    println(flatFuture) // scala.concurrent.impl.Promise$DefaultPromise => Future[Boolean]

    val acceptable = for {
      heatedWater <- heatWater(Water(25))
      okay <- temperatureOkay(heatedWater)
    } yield okay
    println(acceptable) // scala.concurrent.impl.Promise$DefaultPromise => Future[Boolean]
  }
}

package object example_43 {

  implicit val executionContext = scala.concurrent.ExecutionContext.global

  def grind(beans: CoffeeBeans): Future[GroundCoffee] = Future {
    println("start grinding...")
    if (beans == "baked beans") throw GrindingException("are you joking?")
    println("finished grinding...")
    s"ground coffee of $beans"
  }

  def heatWater(water: Water): Future[Water] = Future {
    println("heating the water now")
    println("hot, it's hot!")
    water.copy(temperature = 85)
  }

  def frothMilk(milk: Milk): Future[FrothedMilk] = Future {
    println("milk frothing system engaged!")
    println("shutting down milk frothing system")
    s"frothed $milk"
  }

  def brew(coffee: GroundCoffee, heatedWater: Water): Future[Espresso] = Future {
    println("happy brewing :)")
    println("it's brewed!")
    "espresso"
  }

  def combine(espresso: Espresso, frothedMilk: FrothedMilk): Cappuccino = "cappuccino"

  def prepareCappuccinoSequentially(): Future[Cappuccino] = {
    for {
      ground <- grind("arabica beans")
      water <- heatWater(Water(20))
      foam <- frothMilk("milk")
      espresso <- brew(ground, water)
    } yield combine(espresso, foam)
  }

  def prepareCappuccino(): Future[Cappuccino] = {
    val groundCoffee = grind("arabica beans")
    val heatedWater = heatWater(Water(20))
    val frothedMilk = frothMilk("milk")
    for {
      ground <- groundCoffee
      water <- heatedWater
      foam <- frothedMilk
      espresso <- brew(ground, water)
    } yield combine(espresso, foam)
  }

  def main(args: Array[String]) {
    /*
      Events happened in ordering of declaration:
      start grinding...
      finished grinding...
      heating the water now
      hot, it's hot!
      milk frothing system engaged!
      shutting down milk frothing system
      happy brewing :)
      it's brewed!
      cappuccino

      If you have multiple computations that can be computed in parallel, you need to take care that you
      already create the corresponding Future instances outside of the for comprehension.
    */

    prepareCappuccinoSequentially() onSuccess { case cappuccino =>
      println(cappuccino)
    }
    Thread.sleep(2000) // wait to finish

    /*
      You cannot guarantee the execution ordering :), the events were executed in parallel:
      start grinding...
      milk frothing system engaged!
      heating the water now
      shutting down milk frothing system
      finished grinding...
      hot, it's hot!
      happy brewing :)
      it's brewed!
      cappuccino
    */
    prepareCappuccino() onSuccess { case cappuccino =>
      println(cappuccino)
    }
    Thread.sleep(2000) // wait to finish
  }
}