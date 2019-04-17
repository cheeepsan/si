package models.si

import play.api.libs.json.{Json, OFormat}

case class SiPackage(name: String,
                version: String,
                date: String,
                authors: List[String],
                rounds: List[SiRound]) {

}

object SiPackage {
  implicit val siRoundFormat: OFormat[SiPackage] = Json.format[SiPackage]
}