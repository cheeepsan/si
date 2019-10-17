package models.server.actors

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.io.Tcp._
import akka.io.{IO, Tcp}

object ServerActor {
  def props(conn: InetSocketAddress): Props = {
    Props.create(classOf[ServerActor], conn)
  }
}

class ServerActor(conn: InetSocketAddress) extends Actor {
  import context.system // implicitly used by IO(
  val logger = Logging(context.system, self)
  val tcpActor = IO(Tcp)

  var webSocketActor: Option[ActorRef] = None

  override def preStart(): Unit = {

    tcpActor ! Bind(self, conn, 100)
  }


  def receive = {
    case actor: ActorRef =>
      webSocketActor = Some(actor) //has to be webSocket
      webSocketActor.foreach(_ ! true)
      logger.info("Got message in server actor from actor "+ actor)
    case b @ Bound(localAddress) ⇒
      context.parent ! b
      logger.info("Server actor bound, addr: " + localAddress)

    case CommandFailed(_: Bind) ⇒
      context stop self

    case c @ Connected(remote, local) ⇒
      val connection = sender()
      webSocketActor match {
        case Some(wsActor) =>
          val listener = context.actorOf(ServerListenerActor.props(wsActor))
          val handler = context.actorOf(SimplisticHandler.props(connection, remote, listener))
          connection ! Register(handler)
        case None =>
      }
  }



  override def postRestart(reason: Throwable): Unit = {
    logger.info("RESTARTING")
  }
}

