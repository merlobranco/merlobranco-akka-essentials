package part1recap

import scala.concurrent.Future

object AdvancedRecap extends App {

  // Partial functions
  // Functions that operates ONLY on a subset of a given input domain
  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }
  // This function only operates with 1, 2 and 5, if something else is coming, it will throw an exception

  val pf = (x: Int) => x match {
    case 1 => 42
    case 2 => 65
    case 5 => 999
  }

  val function: (Int => Int) = partialFunction

  val modifiedList = List(1,2,3).map {
    case 1 => 42
    case _ => 0
  }

  // Lifting
  val lifted = partialFunction.lift // total function Int => Option[Int]
  lifted(2) // Some(65)
  lifted(5000) // None because the original partial function does not return anything with 5000

  // orElse. To chain partial function
  val pfChain = partialFunction.orElse[Int, Int] {
    case 60 => 9000
  }

  pfChain(5) // 999 per partialFunction
  pfChain(60) // 9000 per partialFunction
  pfChain(457) // throw a MatchError

  // Type aliases
  type ReceiveFunction = PartialFunction[Any, Unit]

  def receive: ReceiveFunction = {
    case 1 => println("Hello")
    case _ => println("Confused...")
  }

  // Implicits

  implicit val timeout = 3000
  def setTimeout(f: () => Unit)(implicit timeout: Int) = f()

  setTimeout(() => println("Timeout")) // We could omit the extra parameter

  // Implicit conversion
  // 1) Implicit defs
  case class Person(name: String) {
    def greet = s"Hi, my name is $name"
  }

  implicit def formatStringToPerson(string: String): Person = Person(string)
  "Peter".greet
  // formatStringToPerson("Peter").greet

  // 2) Implicit classes
  implicit class Dog(name: String) {
    def bark = println("Bark!")
  }

  "Lassie".bark
  // new Dog("Lassie").bark

  // Organize implicits properly
  // The way the compiler fetch implicits is
  // 1) Local scope
  implicit val inverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  List(1,2,3).sorted // List (3, 2, 1)

  // 2) Imported scope
  import scala.concurrent.ExecutionContext.Implicits.global
  val future = Future {
    println("Hello, future")
  }

  // 3) Companion objects of the types included in the code
  object Person {
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }

  List(Person("Bob"), Person("Alice")).sorted
  // List(Person("Alice"), Person("Bob"))
}
