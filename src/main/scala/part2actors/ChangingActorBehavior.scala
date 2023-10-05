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

  /**
    * Exercises
    * 1 - Recreate the Counter Actor with context.become and NO MUTABLE STATE
    */

  // Domain of the CounterActor
  object CounterActor {
    case object Increment
    case object Decrement
    case object Print
  }

  class CounterActor extends Actor {
    import CounterActor._

    override def receive: Receive = countReceive(0)

    def countReceive(currentCount: Int): Receive = {
        case Increment =>
//          println(s"[$currentCount] incrementing")
          context.become(countReceive(currentCount + 1))
        case Decrement =>
//          println(s"[$currentCount] decrementing")
          context.become(countReceive(currentCount - 1))
        case Print => println(s"[CounterActor] Counter = $currentCount")
    }
  }

  val counterActor = system.actorOf(Props[CounterActor], "counterActor")

  import CounterActor._
  (1 to 5).foreach(_ => counterActor ! Increment)
  (1 to 3).foreach(_ => counterActor ! Decrement)
  counterActor ! Print

  /**
   * Exercises
   * 2 - A simplified voting system
   */

  object Citizen {
    case class Vote(candidate: String)
    case object VoteStatusRequest
    case class VoteStatusReply(candidate: Option[String])
  }

  class Citizen extends Actor {
    import Citizen._

    override def receive: Receive = performVote(None)

    def performVote(vote: Option[String]): Receive = {
      case Vote(candidate) => context.become(performVote(Some(candidate)))
      case VoteStatusRequest => sender() ! VoteStatusReply(vote)
    }
  }

  object VoteAggregator {
    case class AggregateVotes(citizens: Set[ActorRef])
  }

  class VoteAggregator extends Actor {
    import Citizen._
    import VoteAggregator._

    override def receive: Receive = countingVotes(Map(), Set())

    def countingVotes(votes: Map[String, Long], stillWaiting: Set[ActorRef]): Receive = {
      case AggregateVotes(citizens) =>
        context.become(countingVotes(votes, citizens))
        citizens.foreach( citizen => citizen ! VoteStatusRequest)

      case VoteStatusReply(candidate) => candidate match {
        case Some(name) =>
          val newVote = votes.getOrElse(name, 0L) + 1
          checkVotes(votes + (name -> newVote), stillWaiting - sender())

        case None =>
          sender() ! VoteStatusRequest
      }
    }

    private def checkVotes(votes: Map[String, Long], stillWaiting: Set[ActorRef]) = {
      if (stillWaiting.isEmpty)
        votes.foreach(x => println(s"${x._1} -> ${x._2}"))
      context.become(countingVotes(votes, stillWaiting))
    }
  }

  val alice = system.actorOf(Props[Citizen])
  val bob = system.actorOf(Props[Citizen])
  val charlie = system.actorOf(Props[Citizen])
  val daniel = system.actorOf(Props[Citizen])

  import Citizen._
  import VoteAggregator._

  alice ! Vote("Martin")
  bob ! Vote("Jonas")
  charlie ! Vote("Roland")
  daniel ! Vote("Roland")

  val voteAggregator = system.actorOf(Props[VoteAggregator])
  voteAggregator ! AggregateVotes(Set(alice, bob, charlie, daniel))

  /*
    Print the status of the votes

     Martin -> 1
     Jonas -> 1
     Roland -> 2
   */
}
