package models.client.actors

import java.io.File

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging

object ListenerActor {
  def props(clientActor: ActorRef): Props = {
    Props.create(classOf[ListenerActor], clientActor)
  }

}

class ListenerActor(clientActor: ActorRef) extends Actor {

  val log = Logging(context.system, self)
  var webSocketActor: Option[ActorRef] = None

  def receive: PartialFunction[Any, Unit] = {
    case msg: String =>
      msg match {
        case "start" =>
          clientActor ! msg
        case _ =>
          webSocketActor.foreach(_ ! msg)
      }
    case actor: ActorRef =>
      webSocketActor = Some(actor) //has to be webSocket
      webSocketActor.foreach(_ ! true)
    case _ =>
      log.info(":::::::::::::::::::::nothing")

  }
}
