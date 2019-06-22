package models.common.si

import play.api.libs.json.{Json, OFormat}

case class SiTheme(name: String) extends SiObject {


}

object SiTheme {
  def apply(name: String): SiTheme = new SiTheme(name)

  implicit val siThemeFormat: OFormat[SiTheme] = Json.format[SiTheme]
}