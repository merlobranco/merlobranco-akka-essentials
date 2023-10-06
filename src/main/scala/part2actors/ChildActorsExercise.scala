package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChildActorsExercise extends App {

  // Distributed Word Counting

  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(taskId: Int, text: String)
    case class WordCountReply(taskId: Int, count: Int)
  }

  class WordCounterMaster extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case Initialize(nChildren) =>
        println("[master] initializing...")
        val workers = (1 to nChildren).map(child => context.actorOf(Props[WordCounterWorker], s"worker$child"))
        context.become(withWorkers(workers, 1, 1, Map()))
    }

    def withWorkers(workers: Seq[ActorRef], currentWorker: Int, currentTaskId: Int, sendersMap: Map[Int, ActorRef]): Receive = {
      case text: String =>
        println(s"[master] I have received: $text - I will send it to child $currentWorker")
        workers(currentWorker - 1) ! WordCountTask(currentTaskId, text)
        val newWorker = (currentWorker % workers.size) + 1
        val newTaskId = currentTaskId + 1
        val newSendersMap = sendersMap + (currentTaskId -> sender())
        context.become(withWorkers(workers, newWorker, newTaskId, newSendersMap))
      case WordCountReply(id, count) =>
        println(s"[master] I have received a reply for task $id with $count")
        sendersMap(id) ! count
        context.become(withWorkers(workers, currentWorker, currentTaskId, sendersMap - id))
    }
  }


  class WordCounterWorker extends Actor {
    import WordCounterMaster._

    override def receive: Receive = {
      case WordCountTask(id, text) =>
        println(s"[${self.path}] I have received task $id with text: '$text'")
        sender() ! WordCountReply(id, text.split(" ").length)
    }
  }

  class TestActor extends Actor {
    import WordCounterMaster._
    override def receive: Receive = {
      case "start" =>
        val master = context.actorOf(Props[WordCounterMaster], "master")
          master ! Initialize(3)
        val messages =  List(
          "Akka is awesome",
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent mollis elit dolor, ut lobortis elit placerat eget. Sed ut elit at arcu laoreet ornare",
          "Etiam dictum porttitor sapien, quis pretium ante dignissim quis. In eleifend lacus at orci consequat, nec mattis ligula efficitur. Integer gravida bibendum urna cursus porta",
          "Morbi libero lectus, iaculis vel leo eget, fermentum vulputate ipsum. Quisque molestie quam nulla, ac dictum mauris tempor a. Nam varius efficitur ante")
        messages.foreach(message => master ! message)
      case count: Int =>
        println(s"[test actor] I received a reply: $count")
    }
  }

  val system = ActorSystem("wordCounting")

  val testActor = system.actorOf(Props[TestActor], "testActor")
  testActor ! "start"


  /*
    Create WordCounterMaster
    send Initialize(10) to WordCounterMaster
    send "Akka is awesome" to WordCounterMaster
      wcm will send a WordCountTask("...") to one of its children
        child replies with a WordCountReply(3) to the master
      wcm replies with 3 to the sender.

      requester -> wcm -> wcw
      requester <- wcm <-wcw

      // Round robin logic
      // 1,2,3,4,5 and 7 tasks
      // 1,2,3,4,5,1,2
   */
}
