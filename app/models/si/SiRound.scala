package models.si


import play.api.libs.json._
import play.api.libs.functional.syntax._

case class SiTheme(name: String) {


}

object SiTheme {
  def apply(name: String): SiTheme = new SiTheme(name)
  implicit val siThemeFormat: OFormat[SiTheme] = Json.format[SiTheme]
}
case class SiRound(name: String, themes: Map[String, List[SiQuestion]] = Map.empty) {

}

object SiRound {
  implicit val siRoundReads: Reads[SiRound] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "themes").read[Map[String, List[SiQuestion]]]
    )(SiRound.apply _)

  implicit val siRoundWrites: Writes[SiRound] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "themes").write[Map[String, List[SiQuestion]]]
    )(unlift(SiRound.unapply))
}
