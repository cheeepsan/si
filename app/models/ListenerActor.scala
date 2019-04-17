package models

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString

object ListenerActor {
  def props: Props = {
    Props.create(classOf[ListenerActor])
  }

}

class ListenerActor extends Actor {

  val log = Logging(context.system, self)


  def receive: PartialFunction[Any, Unit] = {

    case _ =>
      log.info("nothing")
  
  }

  override def postRestart(reason: Throwable): Unit = {
    log.info("RESTARTING")
  }


}
