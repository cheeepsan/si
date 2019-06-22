package models.server.actors

import java.io.File
import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.io.Tcp._
import akka.io.{IO, Tcp, TcpMessage}
import models.common.si.{SiHtml, SiMessage, SiUser}
import play.api.libs.json.Json
import play.twirl.api.HtmlFormat
import services.SiqParser

object ServerActor {
  def props(conn: InetSocketAddress): Props = {
    Props.create(classOf[ServerActor], conn)
  }
}

class ServerActor(conn: InetSocketAddress) extends Actor {
  import context.system // implicitly used by IO(
  val logger = Logging(context.system, self)

  val tcpActor = IO(Tcp)

  override def preStart(): Unit = {

    tcpActor ! Bind(self, conn, 100)
  }


  def receive = {
    case b @ Bound(localAddress) ⇒
      context.parent ! b
      logger.info("Server actor bound, addr: " + localAddress)

    case CommandFailed(_: Bind) ⇒
      context stop self

    case c @ Connected(remote, local) ⇒
      val connection = sender()
      val handler = context.actorOf(SimplisticHandler.props(connection, remote))

      connection ! Register(handler)
  }



  override def postRestart(reason: Throwable): Unit = {
    logger.info("RESTARTING")
  }
}

