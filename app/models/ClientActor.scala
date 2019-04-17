package models

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props, UntypedAbstractActor}
import akka.event.Logging
import akka.io.Tcp._
import akka.io.{IO, Tcp, TcpMessage}
import akka.util.ByteString

object ClientActor {
  def props(remote: InetSocketAddress, replies: ActorRef): Props = {
    Props.create(classOf[ClientActor], remote, replies)
  }

}

class ClientActor(remote: InetSocketAddress, listener: ActorRef) extends Actor {
  import context.system // implicitly used by IO(
  val log = Logging(context.system, self)
  val tcpActor = IO(Tcp)

//    tcpActor.tell(TcpMessage.connect(remote), self)
  tcpActor ! Connect(remote)

  def receive: PartialFunction[Any, Unit] = {
    case CommandFailed(_: Connect) =>
      listener ! "connect failed"
      context stop self

    case c @ Connected(remote, local) =>
      listener ! c
      val connection = sender()
      connection ! Register(self)
      context become {
        case data: ByteString =>
          connection ! Write(data)
        case CommandFailed(w: Write) =>
          // O/S buffer was full
          listener ! "write failed"
        case Received(data) =>
          listener ! data
        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          listener ! "connection closed"
          context stop self
      }
    case _ =>
      log.info("nothing")
  
  }

  override def postRestart(reason: Throwable): Unit = {
    log.info("RESTARTING")
  }


}
