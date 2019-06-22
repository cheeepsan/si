package models.common.si
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class SiRound(name: String, themes: Map[String, List[SiQuestion]] = Map.empty) extends SiObject {

}

object SiRound {

  implicit val themesMapWrites: Writes[Map[String, List[SiQuestion]]] = Writes[Map[String, List[SiQuestion]]] {
    x =>
      val map = x.map {
        r =>
          Map(r._1 -> Json.toJson(r._2))
      }.flatten.toMap
      Json.toJson(map)
  }

  implicit val siRoundReads: Reads[SiRound] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "themes").read[Map[String, List[SiQuestion]]]
    ) (SiRound.apply _)

  implicit val siRoundWrites: Writes[SiRound] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "themes").write[Map[String, List[SiQuestion]]]
    ) (unlift(SiRound.unapply))

  implicit val listSiRoundWrites: Writes[List[SiRound]] = Writes[List[SiRound]] {
    list =>
      Json.toJson(list.map (SiObject.siObjectWrites.writes(_)))
  }

}
