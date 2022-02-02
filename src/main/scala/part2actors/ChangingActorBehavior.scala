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

  class StatelessFussyKid extends Actor {
    import FussyKid._
    import Mom._

    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false) // Change my receive handler to sadReceive
      case Food(CHOCOLATE) => // Stay happy
      case Ask(_) => sender() ! KidAccept
    }
    def sadReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive, false)
      case Food(CHOCOLATE) => context.unbecome
      case Ask(_) => sender() ! KidReject
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
        kid ! Food(VEGETABLE)
        kid ! Food(CHOCOLATE)
        kid ! Food(CHOCOLATE)
        kid ! Ask("Do you want to play?")
      case KidAccept => println("Yay, my kid is happy!")
      case KidReject => println("My kid is sad, but unless he is healthy!")
    }
  }

  val system = ActorSystem("changingActorBehaviourDemo")
  val mom = system.actorOf(Props[Mom], "momActor")
  val fussyKid = system.actorOf(Props[FussyKid], "fussyKidActor")
  val statelessFussyKid = system.actorOf(Props[StatelessFussyKid], "statelessFussyKidActor")

  mom ! Mom.MomStart(fussyKid)

  mom ! Mom.MomStart(statelessFussyKid)

  /*
    With context.become(sadReceive, true)
      Food(veg) -> message handler turns to sadReceive
      Food(chocolate) -> become happyReceive

    With context.become(sadReceive, false)

      Food(veg) -> stack.push(sadReceive)
      Food(chocolate) -> stack.push(happyReceive)

    Stack:
      1. happyReceive   ->    1. sadReceive     ->    1. happyReceive
                              2. happyReceive         2. sadReceive
                                                      3. happyReceive

    When an actor needs to handle a message, akka will call the top most message handler onto the stack
    If the stack happens to be empty, the akka will call the plane received method
   */

  /*
    New Behaviour (Using unbecome)

    Food(veg)
    Food(veg)
    Food(choco)
    Food(choco)

    Stack:
      1. sadReceive     ->    1. sadReceive     ->    1. sadReceive     ->    1. happyReceive
      2. happyReceive         2. sadReceive     ->    2. happyReceive
                              3. happyReceive
  */


}
