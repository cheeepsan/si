package models

import akka.actor.{AbstractActor, Actor, Props, UntypedAbstractActor}
import akka.event.Logging
import akka.io.Tcp._
import akka.io.TcpMessage
import akka.util.ByteString

class SimplisticHandler extends Actor {

  val log = Logging.getLogger(context.system, this)

  import akka.io.Tcp._
  def receive = {
    case Received(data) ⇒ sender() ! Write(data)
    case PeerClosed     ⇒ context stop self
  }
}
