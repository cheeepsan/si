package models.si

import play.api.libs.json.{Json, OFormat}

case class SiMessage(message: String, user: SiUser, dataObjectType: String, data: SiObject) {

}
object SiMessage {
  implicit val siMessageFormat: OFormat[SiMessage] = Json.format[SiMessage]
}