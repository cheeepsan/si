package models.common.si

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class SiMessage(message: String, user: SiUser, dataObjectType: String, data: SiObject) {
  def apply(message: String, user: SiUser, dataObjectType: String, data: SiObject): SiMessage = new SiMessage(message, user, dataObjectType, data)


  def toJsonAndStringify: String = Json.stringify(toJson)
  def toJson: JsValue = Json.toJson(this)
}
object SiMessage {
  def applyJson(json: JsValue): Option[SiMessage] = json.validate[SiMessage].asOpt
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
      * ...
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

case class SiHtml(html: String) extends SiObject {
  def apply(html: String): SiHtml = SiHtml(html)
}
object SiHtml {
  implicit val siHtmlFormat: OFormat[SiHtml] = Json.format[SiHtml]
}

case class SiText(text: Option[String]) extends SiObject {
  def apply(text: Option[String]): SiText = SiText(text)
}
object SiText {
  implicit val siTextFormat: OFormat[SiText] = Json.format[SiText]
}