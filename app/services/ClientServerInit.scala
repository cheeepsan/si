package services

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.Inject
import javax.inject._
import models.{ClientActor, ListenerActor, ServerActor}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ClientServerInit @Inject()() {
  val conn = new InetSocketAddress("127.0.0.1", 9090)
  val serverActorSystem = ActorSystem.create("ServerActorSystem")
  val clientActorSystem = ActorSystem.create("ClientActorSystem")


  def server = {

    val server: ActorRef = serverActorSystem.actorOf(ServerActor.props(conn), "serverActor")
  }

  def client = {
    val listener = clientActorSystem.actorOf(ListenerActor.props, "listener")
    val clientActor = clientActorSystem.actorOf(ClientActor.props(conn, listener), "clientActor")
  }

  def term = {
    Await.result(serverActorSystem.terminate(), Duration.Inf)
    Await.result(clientActorSystem.terminate(), Duration.Inf)
  }
}
