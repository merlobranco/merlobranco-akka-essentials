package part3testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class BasicSpec extends TestKit(ActorSystem("BasicSpec")) with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._

  "A simple actor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "hello, test"
      echoActor ! message

      /*
        If we want to configure the waiting period of time we should modify this parameter: akka.test.single-expect-default
       */
      expectMsg(message)
    }
  }

  "A Black Hole actor" should {
    "send back the same message" in {
      val blackHoleActor = system.actorOf(Props[BlackHoleActor])
      val message = "hello, test"
      blackHoleActor ! message

      expectNoMessage(1 second)
    }
  }

  // Message assertions
  "A lab test actor" should {
    val labTestActor = system.actorOf(Props[LabTestActor])
    "turn a string into uppercase" in {
      labTestActor ! "I love akka"
      val reply = expectMsgType[String]

      assert(reply == "I LOVE AKKA")
    }

    "reply to a greeting" in {
      labTestActor ! "greeting"
      expectMsgAnyOf("hi", "hello")
    }

    "reply with favorite tech" in {
      labTestActor ! "favoriteTech"
      expectMsgAllOf("scala", "akka")
    }

    "reply with cool tech in a different way" in {
      labTestActor ! "favoriteTech"
      val messages = receiveN(2) // Seq(Any)

      // Free to do more complicated assertions
    }

    "reply with cool tech in a fancy way" in {
      labTestActor ! "favoriteTech"

      expectMsgPF() {
        case "scala" => // Only care that the Partial Function is defined
        case "akka" =>
      }
    }
  }
}

object BasicSpec {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  class BlackHoleActor extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {
    val random = new Random()
    override def receive: Receive = {
      case "greeting" => if (random.nextBoolean()) sender() ! "hi" else sender() ! "hello"
      case "favoriteTech" =>
        sender() ! "scala"
        sender() ! "akka"
      case message: String => sender() ! message.toUpperCase()
    }
  }
}
