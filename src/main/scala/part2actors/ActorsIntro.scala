package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {

  // Part1 - Actor system
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // Part 2 - Create actors
  // Word Count Actor
  class WordCountActor extends Actor {
    // Internal data
    var totalWords = 0

    // Behaviour
    override def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[Word Counter] I have received: ${message}")
        totalWords += message.split(" ").length
      case msg=> println(s"[Word Counter] I cannot understand ${msg.toString}")
    }
  }

  // Part 3 - Instantiate our actor
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

  // Part 4 - Communicate with our actor
  wordCounter ! "I am learning Akka and it's pretty damn cool!" // "Tell"
  anotherWordCounter ! "A different message"

  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "Hi" => println(s"Hi my name is, $name")
    }
  }

  // Creating an actor of a class with constructor arguments
  val person = actorSystem.actorOf(Props(new Person("Bob")))
  person ! "Hi"
}
