package models.common.si

import play.api.libs.json.{Json, OFormat}

case class SiUser(id: Long, name: String) extends SiObject {

}

object SiUser {
  implicit val siUserFormat: OFormat[SiUser] = Json.format[SiUser]
}