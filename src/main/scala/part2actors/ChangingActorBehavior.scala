package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChangingActorBehavior extends App {

  object FussyKid {
    case object KidAccept
    case object KidReject
    val HAPPY = "veggies"
    val SAD = "sad"
  }

  class FussyKid extends Actor {
    import FussyKid._
    import Mom._

    // Internal state of the kid
    var state = HAPPY

    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY) sender() ! KidAccept
        else sender() ! KidReject
    }
  }

  object Mom  {
    case class MomStart(kid: ActorRef)
    case class Food(food: String)
    case class Ask(message: String) // Do you want to play?
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }

  class Mom extends Actor {
    import Mom._
    import FussyKid._

    override def receive: Receive = {
      case MomStart(kid) =>
        kid ! Food(VEGETABLE)
        kid ! Ask("Do you want to play?")
      case KidAccept => println("Yay, my kid is happy!")
      case KidReject => println("My kid is sad, but unless he is healthy!")
    }
  }

  val system = ActorSystem("changingActorBehaviourDemo")
  val mom = system.actorOf(Props[Mom], "momActor")
  val fussyKid = system.actorOf(Props[FussyKid], "fussyKidActor")

  mom ! Mom.MomStart(fussyKid)

}
