package models.server.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging

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
    case _ =>
      logger.info(":::::::::::::::::::::nothing in server")
  }
}
