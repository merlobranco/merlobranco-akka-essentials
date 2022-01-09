package part1recap

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object MultithreadingRecap extends App {

  // Creating threads on the JVM
  val aThread = new Thread(() => println("I am running in parallel"))
//  val aThread = new Thread(new Runnable {
//    override def run(): Unit = () => println("I am running in parallel")
//  })

  aThread.start()
  aThread.join() // Wait for a thread to finish

  val threadHello = new Thread(() => (1 to 1000).foreach(_ => println("Hello")))
  val threadGoodbye = new Thread(() => (1 to 1000).foreach(_ => println("Goodbye")))

  threadHello.start()
  threadGoodbye.start()

  // The problem with threads. Different runs produce different results!

  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = this.amount -= money

    def safeWithdraw(money: Int) = this.synchronized {
      this.amount -= money
    }
  }

  /*
    BA (10000)

    T1 -> withdraw 1000
    T2 -> withdraw 2000

    T1 -> this.amount = this.amount - ... // PREEMPTED by the OS
    T2 -> this.amount = this.amount - 2000 = 8000
    T1 -> - 1000 = 9000
    => Final result => 9000

    Race Condition

    this.amount = this.amount - 1000 is NOT ATOMIC
   */

  // Inter-thread communication on the JVM
  // wait - notify mechanism

  // Scala Futures

  val future = Future {
    // long computation - it will be evaluated on a different thread
    42
  }

  // Callbacks
  future.onComplete {
    case Success(42) => println("I found meaning of life")
    case Failure(_) => println("Something happened with the meaning of life")
  }

  val aProccessedFuture = future.map(_ + 1) // Future with 43
  val aFlatFuture = future.flatMap { value =>
    Future(value + 2)
  } // Future with 44

  val filteredFuture = future.filter(_ % 2 == 0) // NoSuchElementException

  // For comprehensions
  val aNonsenseFuture = for {
    meaningOfLife <- future
    filteredMeaning <- filteredFuture
  } yield meaningOfLife + filteredMeaning

  // andThen, recover/recoverWith

  // Promises
}
