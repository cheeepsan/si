package models.server.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import models.common.si.{SiMessage, SiText, SiUser}
import play.api.libs.json.Json

object ServerListenerActor {
  def props(webSocketActor: ActorRef): Props = {
    Props.create(classOf[ServerListenerActor], webSocketActor)
  }
}

class ServerListenerActor(webSocketActor: ActorRef) extends Actor {
  var handlerActor: Option[ActorRef] = None

  val logger = Logging(context.system, self)
  webSocketActor ! self

  def receive: PartialFunction[Any, Unit] = {
    case actorRef: ActorRef =>
      logger.info("In server listener actor, new actor message. Maybe handler: " + actorRef )
      handlerActor = Some(actorRef) //handler???
      //handlerActor.foreach(_ ! true)
    case msg: String =>
      Json.parse(msg).validate[SiMessage].asOpt match {
        case Some(message) => receiveJsonMessage(message)
        case None => receiveTextMessage(msg)
      }
    case _ =>
      logger.info(":::::::::::::::::::::nothing in server")
  }

  def receiveTextMessage(msg: String) = msg match {
    case "registered" => logger.info("Registered in listener")
    case _ => logger.info("nothing")
  }

  def receiveJsonMessage(message: SiMessage): Unit = message.dataObjectType match {
    case "SiText" =>
      receiveSiText(message)
    case _ => logger.info("nothing")
  }

  def receiveSiText(textMessage: SiMessage): Unit = textMessage.message match {
    case "start" =>
      logger.info("start message")
    case "register" =>
      logger.info("ServerListenerActor::::registering")
      webSocketActor ! textMessage.toJsonAndStringify
    case "selectQuestion" =>
    case _ =>
  }
}
