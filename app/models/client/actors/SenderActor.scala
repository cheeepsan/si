package models.client.actors

import akka.actor.{Actor, Props}

object SenderActor {
  def props(): Props = {
    Props.create(classOf[SenderActor])
  }

}

class SenderActor extends Actor {


  override def receive: Receive = {

    case msg:String =>
      ""
  }
}
