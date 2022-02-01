package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.util.{Failure, Success}

object ActorCapabilities extends App {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi!" => sender() ! "Hello, there!" // Replying to a message
      case message: String => println(s"[${self}] I have received: $message from ${sender()}")
      case number: Int => println(s"[Simple actor] I have received a NUMBER: $number")
      case SpecialMessage(contents) => println(s"[Simple actor] I have received something SPECIAL: $contents")
      case SendMessageToYourself(content) =>
        self ! content
      case SayHiTo(ref) => ref ! "Hi!" // alice is being passed as the sender [*]
      case WirelessPhoneMessage(content, ref) => ref forward (content + "s") // I keep the original sender of the Wireless Phone Message
    }
  }

  val system = ActorSystem("actorSystemCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "Hello, Actor"

  // 1 - Messages can be of any type
  //  a) Messages must be immutable
  //  b) Messages must be SERIALIZABLE
  //  In practice use Case Classes and Objects
  simpleActor ! 42

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("Some special content")

  // 2 - Actors have information about their context and about themselves
  // context.self same as  this in the object oriented paradigm
  // We could use context.self or self for sending messages to ourselves

  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor I am a proud of it")

  // 3 - Actor can REPLY to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob) //[*]

  // 4 - Death letter
  alice ! "Hi!" // Replay to me (But there is no sender)
  // A dead Letter will be triggered, the message "Hello, there!" will be sent but no one will receive it
  // Death letter is a fake actor inside akka that receives messages that were no sent to anyone
  // Kind of Garbage pull of messages

  // 5 - Forwarding messages from one actor to other
  // Daniel -> Alice -> Bob
  // Forwarding = sending a message with the ORIGINAL sender

  case class WirelessPhoneMessage(content: String, ref: ActorRef)
  alice ! WirelessPhoneMessage("Hi", bob) // The original sender here is noSender (deathLetters)

  /**
   * Exercises
   *
   * 1. A Counter actor
   *  - Increment
   *  - Decrement
   *  - Print
   *
   *  2. A Bank Account as an actor
   *    Receives:
   *      - Deposit an amount
   *      - Withdraw an amount
   *      - Statement
   *    Replies
   *      - Success
   *      - Failure
   *
   *  Interacts with other kind of actor
   */

  // Domain of the CounterActor
  object CounterActor {
    case object Increment
    case object Decrement
    case object Print
  }

  class CounterActor extends Actor {
    import CounterActor._
    var counter = 0

    override def receive: Receive = {
      case Increment => counter += 1
      case Decrement => counter -= 1
      case Print => println(s"[CounterActor] Counter = $counter")
    }
  }

  val counterActor = system.actorOf(Props[CounterActor], "counterActor")

  import CounterActor._
  (1 to 5).foreach(_ => counterActor ! Increment)
  (1 to 3).foreach(_ => counterActor ! Decrement)
  counterActor ! Print

  object BankActor {
    case class Deposit(amount: Long = 0)
    case class Withdraw(amount: Long = 0)
    case object Statement

    case class TransactionSuccess(message: String)
    case class TransactionFailure(reason: String)
  }

  class BankActor extends Actor {
    import BankActor._

    var balance = 0L

    override def receive: Receive = {
      case Deposit(amount) =>
        if (amount < 0)
          sender() ! TransactionFailure(s"[BackActor] You cannot add a negative amount: $amount to your balance")
        else {
          balance += amount
          sender() ! TransactionSuccess(s"[BackActor] Amount = $amount added to your balance")
        }
      case Withdraw(amount) =>
        if (amount < 0)
          sender() ! TransactionFailure(s"[BackActor] You cannot reduce a negative amount: $amount to your balance")
        else if (balance < amount)
          sender() ! TransactionFailure(s"[BackActor] You cannot reduce the amount: $amount to your balance")
        else {
          balance -= amount
          sender() ! TransactionSuccess(s"[BackActor] Amount = $amount reduced to your balance")
        }
      case Statement =>
        sender() ! s"[BackActor] You balance is $balance"
    }
  }

  object ATMActor {
    case class PerformActions(account: ActorRef)
  }

  class ATMActor extends Actor {
    import ATMActor._
    import BankActor._

    override def receive: Receive = {
      case PerformActions(bankActor) =>
        bankActor ! Deposit(2000)
        bankActor ! Withdraw(500)
        bankActor ! Statement
      case message => println(message.toString)
    }
  }

  val bankActor = system.actorOf(Props[BankActor], "bankActor")
  val atmActor = system.actorOf(Props[ATMActor], "atmActor")

  import ATMActor._
  atmActor ! PerformActions(bankActor)
}
