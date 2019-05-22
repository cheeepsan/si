package models.actors

import java.io.File

import akka.actor.{Actor, ActorRef, Props}
import models.si._
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.twirl.api.HtmlFormat
import services.SiqParser

object MyWebSocketActor {
  def props(out: ActorRef) = Props(new MyWebSocketActor(out))
}

class MyWebSocketActor(out: ActorRef) extends Actor {
  val logger = Logger

  def receive = {
    case msg: String =>
      val jsonMessage = Json.parse(msg).validate[SiMessage].asOpt
      jsonMessage match {
        case Some(message) =>
          message.dataObjectType match {
            case "SiText" =>
              message.message match {
                case "start" =>
                  logger.info("start message")
                  out ! Json.stringify(this.json)
                case "chooseQuestion" =>
                  out ! SiMessage("removeQuestion", new SiUser(1, ""), "SiText", message.data.asInstanceOf[SiText]).toJsonAndStringify
              }
            case _ => logger.info("nothing")
          }
        case None =>
          logger.info("was unable to parse message: " + msg)
      }
      logger.info(msg)
    case _ => logger.info("hi")
  }

  def json = {
    val destDir = new File("F:\\workspace\\si\\siClient\\pack")
    val parser = new SiqParser
    val s = parser.process(destDir)

    val b: HtmlFormat.Appendable = views.html.siRenderers.round.round(s.rounds.head)
    val siHtml = SiHtml(b.body)
    val className =  siHtml.getClass.getTypeName.split("\\.").last
    logger.info("Sending round")
    Json.toJson(new SiMessage("round", new SiUser(1, ""), className, siHtml))
  }

  override def postStop(): Unit = {
    logger.info("ACTOR STOPPED")
  }
}