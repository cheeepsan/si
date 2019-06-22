package services

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.Inject
import javax.inject._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


@Singleton
class ClientServerInit @Inject()() {
  private val serverActorSystem: ActorSystem = ActorSystem.create("ServerActorSystem")
  private val clientActorSystem: ActorSystem = ActorSystem.create("ClientActorSystem")


  def server = {
//
//    val server: ActorRef = serverActorSystem.actorOf(ServerActor.props(conn), "serverActor")
    serverActorSystem
  }

  def client = {
//    val listener = clientActorSystem.actorOf(ListenerActor.props, "listener")
//    val clientActor = clientActorSystem.actorOf(ClientActor.props(conn, listener), "clientActor")
    clientActorSystem
  }

  def term = {
    Await.result(serverActorSystem.terminate(), Duration.Inf)
    Await.result(clientActorSystem.terminate(), Duration.Inf)
  }
}
