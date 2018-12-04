import java.time.Instant

import _root_.state.Monoid
import scalaz.State
import state.Monoid._
import scalaz.State._

package object example_state {

  sealed trait TransactionType
  case object DR extends TransactionType
  case object CR extends TransactionType

  sealed trait Currency
  case object USD extends Currency
  case object BRL extends Currency

  case class Money(m: Map[Currency, BigDecimal]) {
    def toBaseCurrency: BigDecimal = ???
  }

  case class Balance(amount: Money = zeroMoney)

  type AccountNumber = String
  type Balances      = Map[AccountNumber, Balance]

  case class Transaction(
      accountNumber: AccountNumber,
      amount: Money,
      transactionType: TransactionType,
      date: Instant
  )

  def updateBalance(transactions: List[Transaction]): State[Balances, Unit] =
    modify { b: Balances =>
      transactions.foldLeft(b) { (account, transaction) =>
        implicitly[Monoid[Balances]].op(account, Map(transaction.accountNumber -> Balance(transaction.amount)))
      }
    }
}

object Main extends App {

  import example_state._

  val transactionsBatch1: List[Transaction] = List(
      Transaction(accountNumber = "a1",
                  amount = Money(Map(USD -> BigDecimal(100))),
                  transactionType = CR,
                  date = Instant.now()),
      Transaction(accountNumber = "a2",
                  amount = Money(Map(USD -> BigDecimal(100))),
                  transactionType = CR,
                  date = Instant.now()),
      Transaction(accountNumber = "a1",
                  amount = Money(Map(BRL -> BigDecimal(500000))),
                  transactionType = CR,
                  date = Instant.now())
  )

  val balancesState1 = updateBalance(transactionsBatch1) run Map.empty
  println(balancesState1._1) // Map(a1 -> Balance(Money(Map(USD -> 100, BRL -> 500000))), a2 -> Balance(Money(Map(USD -> 100))))

  val transactionsBatch2: List[Transaction] = List(
      Transaction(accountNumber = "a3",
                  amount = Money(Map(USD -> BigDecimal(100))),
                  transactionType = CR,
                  date = Instant.now()),
      Transaction(accountNumber = "a2",
                  amount = Money(Map(BRL -> BigDecimal(200))),
                  transactionType = CR,
                  date = Instant.now()),
      Transaction(accountNumber = "a2",
                  amount = Money(Map(USD -> BigDecimal(400))),
                  transactionType = CR,
                  date = Instant.now())
  )

  val balancesState2 = updateBalance(transactionsBatch2) run balancesState1._1
  println(balancesState2._1) // Map(a1 -> Balance(Money(Map(USD -> 100, BRL -> 500000))), a2 -> Balance(Money(Map(USD -> 500, BRL -> 200))), a3 -> Balance(Money(Map(USD -> 100))))
}
