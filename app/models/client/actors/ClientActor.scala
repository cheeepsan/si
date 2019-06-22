package models.client.actors

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.remote.Ack
import akka.util.ByteString.{ByteString1, ByteStrings}
import akka.util.{ByteString, CompactByteString}

object ClientActor {
  def props(remote: InetSocketAddress): Props = {
    Props.create(classOf[ClientActor], remote)
  }

}

class ClientActor(remote: InetSocketAddress) extends Actor {
  import context.system // implicitly used by IO(
  val log = Logging(context.system, self)
  val listener: ActorRef = context.system.actorOf(ListenerActor.props(self), "listener")
  val tcpActor = IO(Tcp)

  private var storage = Vector.empty[ByteString]

  tcpActor ! Connect(remote)

  def receive: PartialFunction[Any, Unit] = {
    case CommandFailed(_: Connect) =>
      listener ! "connect failed"
      context stop self
    case c @ Connected(remote, local) =>
      val connection = context.sender()
      connection ! Register(self)
      receiveConnected(connection)
    case msg: String =>
      log.info("RECIEIVED TEXT:::::::::::: " + msg)
    case _ =>
      log.info("nothing")
  
  }

  def receiveConnected(connection: ActorRef) = context become {
    case msg: String =>
      connection ! Write(ByteString(msg))
    case data: ByteString =>
      connection ! Write(data)
    case CommandFailed(w: Write) =>
      // O/S buffer was full
      listener ! "write failed"
    case Received(data: ByteString) =>
      if (data.length == 3 && data.utf8String == "FIN") {
        log.info("Full msg received, vecotr empty")
        listener ! storage.map(_.utf8String).mkString
        storage = Vector.empty
      } else {
        storage :+= data
      }
    case "close" =>
      connection ! Close
    case _: ConnectionClosed =>
      listener ! "connection closed"
      context stop self
  }

  override def postRestart(reason: Throwable): Unit = {
    log.info("RESTARTING")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    context stop self
  }


}
