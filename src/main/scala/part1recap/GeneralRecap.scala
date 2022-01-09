package part1recap

import scala.util.Try

object GeneralRecap extends App {

  val aCondition: Boolean = false

  var aVariable = 42
  aVariable += 1

  // Expressions
  val aConditionVal = if (aCondition) 42 else 65

  // Code block
  val aCodeBlock = {
    if (aCondition) 74
    56
  }

  // Types
  // Unit: do something but does not return something meaningful, side effects
  val theUnit = println("Hello, Scala")

  def aFunction(x: Int) = x + 1

  // Recursion - TAIL recursion
  def factorial(n: Int, acc: Int): Int =
    if (n <= 0) acc
    else factorial(n - 1, acc * n)

  // OOP Object Oriented Programming
  class Animal
  class Dog extends Animal
  val aDog: Animal = new Dog

  trait Carnivore {
    def eat(a: Animal): Unit
  }

  // We could add as many traits (behaviours) we want
  class Crocodidle extends Animal with Carnivore {
    override def eat(a: Animal): Unit = println("Crunch!")
  }

  // Method Notations
  val aCrocodile = new Crocodidle
  aCrocodile.eat(aDog)
  aCrocodile eat aDog

  // Anonymous classes
  val aCarnivore = new Carnivore {
    override def eat(a: Animal): Unit = println("Roar!")
  }

  aCarnivore eat aDog

  // Generics
  abstract class MyList[+A]

  // Companion objects
  object MyList

  // Case classes
  case class Person(name: String, age: Int)

  // Exceptions
  val aPotential = try {
    throw new RuntimeException("I'm innocent, I swear!") // Returns Nothing
  } catch {
    case e: Exception => "I caught an exception"
  } finally {
    // Side effects, it's happening no matter what
    println("Some logs")
  }

  // Functional programming
  // The way Scala made it to run on JVM, since Java is an Object Oriented language, and Scala is Functional oriented, is making the functions objects
  val incrementer = new Function1[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  val incremented = incrementer(42)
  // incrementer.apply(42)

  val anonymousIncrementer = (x: Int) => x + 1
  // Int => Int === Function1[Int, Int]

  // FP Functional Programming is all about working with functions as first-class
  List(1,2,3).map(incrementer)
  // map is a HOF Higher Order Function, it takes a function and return another function as a result

  // For comprehensions
  val pairs = for {
    num <- List(1, 2, 3, 4)
    char <- List('a', 'b', 'c', 'd')
  } yield num + "-" + char
  // List(1,2,3,4).flatMap(num => List('a', 'b', 'c', 'd').map(char => num + "-" + char))

  // Seq, Array, List, Vectors, Map, Tuples, Sets

  // Collections
  // Option and Try
  val anOption = Some(2)
  val aTry = Try {
    throw new RuntimeException
  }

  // Pattern matching
  val unknown = 2
  val order = unknown match {
    case 1 => "First"
    case 2 => "Second"
    case _ => "Unknown"
  }

  val bob = Person("Bob", 22)
  val greeting = bob match {
    case Person(n, _) => s"Hi, my name is $n"
    case _ => ""
  }

  // ALL THE PATTERNS
}
