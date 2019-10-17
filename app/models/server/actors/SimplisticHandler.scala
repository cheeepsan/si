package models.server.actors

import java.io.File
import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.io.Tcp.Event
import akka.stream.ActorMaterializer
import akka.util.ByteString
import models.common.si.{SiMessage, SiUser}
import play.api.libs.json.Json
import services.SiqParser

//https://github.com/akka/akka/blob/v2.5.25/akka-docs/src/test/scala/docs/io/EchoServer.scala
object SimplisticHandler {

  final case class Ack(offset: Int) extends Event

  def props(connection: ActorRef, remote: InetSocketAddress, serverListenerActor: ActorRef): Props = {
    Props.create(classOf[SimplisticHandler], connection, remote, serverListenerActor)
  }
}

class SimplisticHandler(connection: ActorRef, remote: InetSocketAddress, serverListenerActor: ActorRef) extends Actor {
  import SimplisticHandler.Ack
  import akka.io.Tcp._

  implicit val materializer = ActorMaterializer()
  val logger = Logging.getLogger(context.system, this)
  serverListenerActor ! self

  def receive = writing

  def writing: Receive = {
    case Received(data) =>
      logger.info("Server handler received, sending back: " + data.utf8String)
      processReceivedData(data)(convertByteString) match {
        case Right(responseData) =>
          val dataToWrite = ByteString(responseData)
          connection ! Write(dataToWrite, Ack(currentOffset))
          buffer(dataToWrite)
        case Left(string) =>
          serverListenerActor ! string
      }
      /*data.utf8String match {
        case "start" =>
          val dataToWrite = ByteString(this.json.toJsonAndStringify)
          connection ! Write(dataToWrite, Ack(currentOffset))
          buffer(dataToWrite)
        case _ =>
      }*/
    case Ack(ack) =>
      logger.info("Ack received in handler: " + ack)
      if (ack == storageOffset) {
        connection ! Write(ByteString("FIN"), NoAck)
      } else {
        acknowledge(ack)
      }
    case CommandFailed(Write(_, Ack(ack))) =>
      logger.info("Command failed in server handler")
      connection ! ResumeWriting
      context.become(buffering(ack))
    case PeerClosed =>
      logger.info("Conn closed in context")
      if (storage.isEmpty) context.stop(self)
      else context.become(closing)
  }


  /**
    * Right to client
    * Left to browser and eventually to websocket
    * @param data
    * @param byteString
    * @return
    */
  def processReceivedData(data: ByteString)
                         (byteString: ByteString => Either[String, SiMessage]): Either[String, String]
  = byteString(data) match {
    case Right(value: SiMessage) =>
      logger.info("siMessage obj recieved")
      value.message match {
        case "register" =>
          logger.info("HANDLER:::::register")
          Left(value.toJsonAndStringify)
        case _ => Left("")
      }
    case Left(string: String) =>
      string match {
        case "start" => Right(this.json.toJsonAndStringify)
        case _ => Left("")
      }
  }

  def convertByteString(data: ByteString) = {
    val string = data.utf8String
    logger.info("Converting bytestring to json or string")
    Json.parse(string).validate[SiMessage].asOpt match {
      case Some(siMessage) =>
        Right(siMessage)
      case None =>
        Left(string)
    }
  }

  def buffering(nack: Int): Receive = {
    var toAck = 10
    var peerClosed = false

    {
      case Received(data)         => buffer(data)
      case WritingResumed         => writeFirst()
      case PeerClosed             => peerClosed = true
      case Ack(ack) if ack < nack => acknowledge(ack)
      case Ack(ack) =>
        acknowledge(ack)
        if (storage.nonEmpty) {
          if (toAck > 0) {
            // stay in ACK-based mode for a while
            writeFirst()
            toAck -= 1
          } else {
            // then return to NACK-based again
            writeAll()
            context.become(if (peerClosed) closing else writing)
          }
        } else if (peerClosed) context.stop(self)
        else context.become(writing)
    }
  }

  /**
    * closing
    * @return
    */
  def closing: Receive = {
    case CommandFailed(_: Write) =>
      connection ! ResumeWriting
      context.become({

        case WritingResumed =>
          writeAll()
          context.unbecome()

        case ack: Int => acknowledge(ack)

      }, discardOld = false)

    case Ack(ack) =>
      acknowledge(ack)
      if (storage.isEmpty) context.stop(self)
  }

  //https://doc.akka.io/docs/akka/current/io-tcp.html ACK
  private var storageOffset = 0
  private var storage = Vector.empty[ByteString]
  private var stored = 0L
  private var transferred = 0L

  private val maxStored = 100000000L
  private val highWatermark = maxStored * 5 / 10
  private val lowWatermark = maxStored * 3 / 10
  private var suspended = false

  private def currentOffset = storageOffset + storage.size

  private def writeFirst(): Unit = {
    connection ! Write(storage(0), Ack(storageOffset))
  }

  private def writeAll(): Unit = {
    for ((data, i) <- storage.zipWithIndex) {
      connection ! Write(data, Ack(storageOffset + i))
    }
  }

  /**
    * Helpers
    * @param data
    */
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

  private def acknowledge(ack: Int): Unit = {
    require(ack == storageOffset, s"received ack $ack at $storageOffset")
    require(storage.nonEmpty, "storage was empty")

    val size = storage(0).size
    stored -= size
    transferred += size

    storage = storage.drop(1)

    storageOffset += 1
    storage = storage.drop(1)

    if (suspended && stored < lowWatermark) {
      logger.debug("resuming reading")
      connection ! ResumeReading
      suspended = false
    }
  }

  def json = {
    val destDir = new File("/home/chipson/workspace/siGameFree/Vse_podryad_1")
    val parser = new SiqParser
    val s = parser.process(destDir)
    val round = s.rounds.head
    val className = round.getClass.getTypeName.split("\\.").last
    logger.info("Sending round")
    new SiMessage("round", new SiUser(1, ""), className, round)
  }
}
/*

processReceivedData(data)(convertByteString) match {
   case Right(json) =>
     streamChunkedData(json)
   case Left(string) =>
     connection ! Write(ByteString(string))
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
*/