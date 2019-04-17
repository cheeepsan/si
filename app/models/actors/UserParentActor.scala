package models.actors

import javax.inject.Inject
import akka.actor._
import akka.event.LoggingReceive
import akka.stream.scaladsl._
import akka.util.Timeout
import play.api.Configuration
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
class UserParentActor @Inject()(
                                configuration: Configuration)
                               (implicit ec: ExecutionContext)
  extends Actor with InjectedActorSupport with ActorLogging {


  import UserParentActor._
  import akka.pattern.{ask, pipe}

  implicit val timeout = Timeout(2.seconds)



  override def receive: Receive = LoggingReceive {
    case Create(id) =>
      val name = s"userActor-$id"
      log.info(s"Creating user actor $name with default stocks ")

      pipe(Future.successful()) to sender()
  }
}

object UserParentActor {
  case class Create(id: String)
}