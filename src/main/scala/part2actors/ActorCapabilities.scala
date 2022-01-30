package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

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

}
