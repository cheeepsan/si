package models.si


import play.api.libs.json._
import play.api.libs.functional.syntax._

case class SiTheme(name: String) extends SiObject {


}

object SiTheme {
  def apply(name: String): SiTheme = new SiTheme(name)

  implicit val siThemeFormat: OFormat[SiTheme] = Json.format[SiTheme]
}

case class SiRound(name: String, themes: Map[String, List[SiQuestion]] = Map.empty) extends SiObject {

}

object SiRound {

  implicit val themesMapWrites: Writes[Map[String, List[SiQuestion]]] = Writes[Map[String, List[SiQuestion]]] {
    x =>
      val map = x.map {
        r =>
          //          val v = r._2.map(SiObject.siObjectWrites.writes(_))
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
