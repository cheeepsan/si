package models

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props, UntypedAbstractActor}
import akka.event.Logging
import akka.io.Tcp._
import akka.io.{IO, Tcp, TcpMessage}

object ServerActor {
  def props(conn: InetSocketAddress): Props = {
    Props.create(classOf[ServerActor], conn)
  }
}

class ServerActor(conn: InetSocketAddress) extends Actor {
  import context.system // implicitly used by IO(
  val log = Logging(context.system, self)

  val tcpActor = IO(Tcp)

  override def preStart(): Unit = {
    tcpActor ! Bind(self, conn, 100)
  }

  def receive = {
    case b @ Bound(localAddress) ⇒
      log.info("Server actor bound")

    case CommandFailed(_: Bind) ⇒ context stop self

    case c @ Connected(remote, local) ⇒
      val handler = context.actorOf(Props[SimplisticHandler])
      val connection = sender()
      connection ! Register(handler)
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit =  {
    super.preRestart(reason, message)
    val conn = new InetSocketAddress("localhost", 9090)
    tcpActor.tell(
      TcpMessage.bind(
        self,
        conn,
        100),
      self)
  }

  override def postRestart(reason: Throwable): Unit = {
    log.info("RESTARTING")
  }
}

