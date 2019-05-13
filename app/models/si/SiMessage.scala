package models.si

import play.api.libs.functional.syntax._
import play.api.libs.json._

trait SiObject

object SiObject {
  implicit val siObjectReads: Reads[SiObject] = new Reads[SiObject] {
  override def reads(json: JsValue): JsResult[SiObject] = {
    json.validate[SiPackage]
      .orElse(json.validate[SiRound])
      .orElse(json.validate[SiTheme])
      .orElse(json.validate[SiQuestion])
  }
}

  implicit val siObjectWrites: Writes[SiObject]  = new Writes[SiObject] {
    override def writes(o: SiObject): JsValue = o match {
      case x:SiPackage => SiPackage.siPackageWrites.writes(x)
      case x:SiRound => SiRound.siRoundWrites.writes(x)
      case x:SiTheme => SiTheme.siThemeFormat.writes(x)
      case x:SiQuestion => SiQuestion.siQuestionWrites.writes(x)
    }
  }

//  implicit val siObjectFormat: OFormat[SiObject] = Json.format[SiObject]
}

case class SiMessage(message: String, user: SiUser, dataObjectType: String, data: SiObject) {
  def apply(message: String, user: SiUser, dataObjectType: String, data: SiObject): SiMessage = new SiMessage(message, user, dataObjectType, data)
}
object SiMessage {

  implicit val siMessageReads: Reads[SiMessage] = (
    (JsPath \ "message").read[String] and
    (JsPath \ "user").read[SiUser] and
    (JsPath \ "dataObjectType").read[String] and
    (JsPath \ "data").read[SiObject]
    /**
      * .map {
      * case x:SiPackage => x.isInstanceOf[SiPackage]
      * case x:SiRound => x.isInstanceOf[SiRound]
      * case x:SiTheme => x.isInstanceOf[SiTheme]
      * case x:SiQuestion => x.isInstanceOf[SiQuestion]
      * }
      */
    )(SiMessage.apply _)

  implicit val siMessageWrites: Writes[SiMessage] = (
    (JsPath \ "message").write[String] and
    (JsPath \ "user").write[SiUser] and
    (JsPath \ "dataObjectType").write[String] and
    (JsPath \ "data").write[SiObject]

    )(unlift(SiMessage.unapply))

  implicit val siMessageFormat: OFormat[SiMessage] = Json.format[SiMessage]
}