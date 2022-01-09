package part1recap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ThreadModelLimitations extends App {

  /*
    Rants
  */

  /**
   * Rant #1: OOP encapsulation is only valid in the SINGLE THREADED MODEL.
   */

  class BankAccount(private var amount: Int) {
    override def toString: String = "" + amount

    def withdraw(money: Int) = this.synchronized {
      this.amount -= money
    }

    def deposit(money: Int) = this.synchronized {
      this.amount += money
    }

    def getAmount = amount
  }

//  val account = new BankAccount(2000)
//  for(_ <- 1 to 1000) {
//    new Thread(() => account.withdraw(1)).start()
//  }
//  for (_ <- 1 to 1000) {
//    new Thread(() => account.deposit(1)).start()
//  }
//  println(account.getAmount)

  // OPP encapsulation is broken in a multithreaded env
  // Solution => Synchronization! Locks to the rescue
  // Some problems are solved, but another ones are introduced like deadlocks, livelocks

  /**
   * Rant #2: Delegating something to a thread is a PAIN
   */

  // We have a running thread and we want to pass a runnable to that thread

  var task: Runnable = null

  val runningThread: Thread = new Thread(() => {
    while (true) {
      while (task == null) {
        runningThread.synchronized {
          println("[Background] waiting for a task...")
          runningThread.wait()
        }
      }
      task.synchronized {
        println("[Background] I have a task")
        task.run()
        task = null
      }
    }
  })

  def delegateToBackgroundThread(r: Runnable) = {
    if (task == null) task = r

    runningThread.synchronized {
      runningThread.notify()
    }
  }

  runningThread.start()
  Thread.sleep(500)
  delegateToBackgroundThread(() => println(42))
  Thread.sleep(1000)
  delegateToBackgroundThread(() => println("This should run in the background"))

  /*
    Other problems:

    How we could send other signals?
    What about if there are multiple background tasks and threads? How we identify which thread gets which tasks?
    From the running task how we identified the thread who gave us the signal?
    What about if the background thread gets stuck? Or crash?
   */

  /*
    We need a data structure which

      - Can safely receive messages
      - Can identify the sender
      - It is easily identifiable
      - Can guards itself against errors
   */

  /**
   * Rant #3: Tracing and dealing with errors in a multithreaded environment is PAINFUL as well
   */

  // 1M numbers in between 10 threads

  val futures = (0 to 9)
    .map(i => 100000 * i until 100000 * (i + 1)) // 0 - 99999, 100000 - 199999, 200000 - 299999 etc
    .map(range => Future {
      if (range.contains(546735)) throw new RuntimeException("Invalid number")
      range.sum
    })

  val sumFuture = Future.reduceLeft(futures)(_ + _) // Future with the sum of all the numbers
  sumFuture.onComplete(println)
}
