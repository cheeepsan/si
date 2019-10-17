package models.client.actors

import akka.actor.{Actor, ActorRef, Props}
import models.common.si._
import play.api.Logger
import play.api.libs.json.Json
import play.twirl.api.HtmlFormat

object MyWebSocketActor {
  def props(out: ActorRef, clientListener: ActorRef) = Props(new MyWebSocketActor(out, clientListener))
}

class MyWebSocketActor(out: ActorRef, clientListener: ActorRef) extends Actor {
  val logger = Logger

  override def preStart(): Unit =  {
    clientListener ! self
  }

  def receive = {
    case msg: String =>
      Json.parse(msg).validate[SiMessage].asOpt match {
        case Some(message) => receiveJsonMessage(message)
        case None => receiveTextMessage(msg)
      }
    case _ => logger.info("hi")
  }

  def receiveTextMessage(msg: String) = msg match {
    case "registered" => logger.info("Registered in listener")
    case _ => logger.info("nothing")
  }

  def receiveJsonMessage(message: SiMessage): Unit = message.dataObjectType match {
    case "SiText" =>
      receiveSiText(message)
    case "SiRound" =>
      out ! roundToHtml(message)
    case _ => logger.info("nothing")
  }


  def receiveSiText(textMessage: SiMessage): Unit = textMessage.message match {
    case "start" =>
      logger.info("start message")
      clientListener ! textMessage.toJsonAndStringify
    case "register" =>
      logger.info("MyWebSocketActor::::::registering")
      clientListener ! textMessage.toJsonAndStringify
    case "selectQuestion" =>
      out ! SiMessage("removeQuestion", new SiUser(1, ""), "SiText", textMessage.data.asInstanceOf[SiText]).toJsonAndStringify
    case _ => out ! "empty"
  }


  def roundToHtml(message: SiMessage) = {

    val b: HtmlFormat.Appendable = views.html.client.siRenderers.round.round(message.data.asInstanceOf[SiRound])
    val siHtml = SiHtml(b.body)
    logger.info("Sending round")
    new SiMessage("round", new SiUser(1, ""), message.dataObjectType, siHtml).toJsonAndStringify
  }

  override def postStop(): Unit = {
    logger.info("ACTOR STOPPED")
  }
}