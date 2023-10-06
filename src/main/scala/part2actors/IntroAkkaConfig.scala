package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {

  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
   * 1 - Inline configuration
   */
  val configString =
    """
      | akka {
      |   loglevel = INFO
      | }
      |""".stripMargin

  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("ConfigurationDemo", ConfigFactory.load(config))
  val actor = system.actorOf(Props[SimpleLoggingActor])
  actor ! "A message to remember 1"

  /**
   * 2 - Config file
   */
  val defaultConfigFileSystem = ActorSystem("DefaultConfigFileDemo")
  val defaultConfigActor = defaultConfigFileSystem.actorOf(Props[SimpleLoggingActor])
  defaultConfigActor ! "A message to remember 2"

  /**
   * 3 - Separate config in the same file
   */
  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigFileSystem = ActorSystem("DefaultConfigFileDemo", specialConfig)
  val specialConfigActor = specialConfigFileSystem.actorOf(Props[SimpleLoggingActor])
  specialConfigActor ! "A message to remember 3"

  /**
   * 4 - Separate config in the another file
   */
  val separateConfig = ConfigFactory.load("secretFolder/secretConfiguration.conf")
  println(s"Separate config log level: ${separateConfig.getString("akka.loglevel")}")

  /**
   * 5 - Different file formats
   * JSON, Properties
   */
  val jsonConfig = ConfigFactory.load("json/jsonConfig.json")
  println(s"Json Config: ${jsonConfig.getString("aJsonProperty")}")
  println(s"Json Config: ${jsonConfig.getString("akka.loglevel")}")

  val propsConfig = ConfigFactory.load("props/propsConfig.properties")
  println(s"Props Config: ${propsConfig.getString("my.simpleProperty")}")
  println(s"Props Config: ${propsConfig.getString("akka.loglevel")}")
}
