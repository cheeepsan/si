package models.common.si

import play.api.libs.functional.syntax._
import play.api.libs.json._



case class SiPackage(name: String,
                version: String,
                date: String,
                authors: List[String],
                rounds: List[SiRound]) extends SiObject {

}

object SiPackage {
  implicit val siPackageReads: Reads[SiPackage] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "version").read[String] and
      (JsPath \ "date").read[String] and
      (JsPath \ "authors").read[List[String]] and
      (JsPath \ "rounds").read[List[SiRound]]

    )(SiPackage.apply _)

  implicit val siPackageWrites: Writes[SiPackage] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "version").write[String] and
      (JsPath \ "date").write[String] and
      (JsPath \ "authors").write[List[String]] and
      (JsPath \ "rounds").write[List[SiRound]]

    )(unlift(SiPackage.unapply))


  implicit val siRoundFormat: OFormat[SiPackage] = Json.format[SiPackage]
}