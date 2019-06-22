package models.server.actors

import java.io.File
import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.io.Tcp.Event
import akka.remote.Ack
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Framing, Sink, Source}
import akka.util.ByteString
import models.common.si.{SiHtml, SiMessage, SiObject, SiUser}
import play.api.libs.json.Json
import services.SiqParser


object SimplisticHandler {
  def props(connection: ActorRef, remote: InetSocketAddress): Props = {
    Props.create(classOf[SimplisticHandler], connection, remote)
  }
}

class SimplisticHandler(connection: ActorRef, remote: InetSocketAddress) extends Actor {
  implicit val materializer = ActorMaterializer()
  val logger = Logging.getLogger(context.system, this)

  case object Ack extends Event
  import akka.io.Tcp._

  def receive = {
    case Received(data) =>
      processReceivedData(data)(convertByteString) match {
        case Right(json) =>
          streamChunkedData(json)
        case Left(string) =>
          sender() ! Write(ByteString(string))
      }
    case PeerClosed => context stop self
  }

  def processReceivedData(data: ByteString)
                         (byteString: ByteString => Either[String, SiMessage])
  = byteString(data) match {
    case Right(value: SiMessage) =>
      logger.info("siMessage obj recieved")
      Left("")
    case Left(string: String) =>
      string match {
        case "start" => Right(this.json)
        case _ => Left("")
      }
  }

  def convertByteString(data: ByteString) = {
    val string = data.utf8String
    Json.toJson(string).validate[SiMessage].asOpt match {
      case Some(siMessage) =>
        Right(siMessage)
      case None =>
        Left(string)
    }
  }

  def streamChunkedData(json: SiMessage) = {
    val byteString = ByteString(json.toJsonAndStringify)
//    val framing = Framing.simpleFramingProtocolEncoder(4)
//    val flow = Flow[ByteString].via(framing)
//    val source = Source.single(byteString).via(flow)
//    source.runWith(Sink.foreach { msg =>
//
//      sender() ! Write(msg)
//
//    })
    contextStuff(byteString)
  }

  def contextStuff(chunk: ByteString) = {
    buffer(chunk)
    sender() ! Write(chunk, Ack)

    context.become({
      case Received(data) => buffer(data)
      case Ack            => acknowledge()
      case PeerClosed     => closing = true
    }, discardOld = false)
  }

  //https://doc.akka.io/docs/akka/current/io-tcp.html ACK
  var storage = Vector.empty[ByteString]
  var stored = 0L
  var transferred = 0L
  var closing = false

  val maxStored = 100000000L
  val highWatermark = maxStored * 5 / 10
  val lowWatermark = maxStored * 3 / 10
  var suspended = false

  private def buffer(data: ByteString): Unit = {
    storage :+= data
    stored += data.size

    if (stored > maxStored) {
      logger.warning(s"drop connection to [$remote] (buffer overrun)")
      context.stop(self)

    } else if (stored > highWatermark) {
      logger.debug(s"suspending reading")
      connection ! SuspendReading
      suspended = true
    }
  }

  private def acknowledge(): Unit = {
    require(storage.nonEmpty, "storage was empty")

    val size = storage(0).size
    stored -= size
    transferred += size

    storage = storage.drop(1)

    if (suspended && stored < lowWatermark) {
      logger.debug("resuming reading")
      connection ! ResumeReading
      suspended = false
    }

    if (storage.isEmpty) {
      connection ! Write(ByteString("FIN"), Ack)
      if (closing) context.stop(self)
      else context.unbecome()
    } else connection ! Write(storage(0), Ack)
  }



  def json = {
    val destDir = new File("F:\\workspace\\si\\siClient\\pack")
    val parser = new SiqParser
    val s = parser.process(destDir)
    val round = s.rounds.head
    val className = round.getClass.getTypeName.split("\\.").last
    logger.info("Sending round")
    new SiMessage("round", new SiUser(1, ""), className, round)
  }
}
