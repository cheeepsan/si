package models.common.si

import play.api.libs.json.{JsResult, JsValue, Reads, Writes}

trait SiObject

object SiObject {
  implicit val siObjectReads: Reads[SiObject] = new Reads[SiObject] {
    override def reads(json: JsValue): JsResult[SiObject] = {
      json.validate[SiPackage]
        .orElse(json.validate[SiRound])
        .orElse(json.validate[SiTheme])
        .orElse(json.validate[SiQuestion])
        .orElse(json.validate[SiHtml])
        .orElse(json.validate[SiText])
    }
  }

  implicit val siObjectWrites: Writes[SiObject]  = new Writes[SiObject] {
    override def writes(o: SiObject): JsValue = o match {
      case x:SiPackage => SiPackage.siPackageWrites.writes(x)
      case x:SiRound => SiRound.siRoundWrites.writes(x)
      case x:SiTheme => SiTheme.siThemeFormat.writes(x)
      case x:SiQuestion => SiQuestion.siQuestionWrites.writes(x)
      case x:SiHtml => SiHtml.siHtmlFormat.writes(x)
      case x:SiText => SiText.siTextFormat.writes(x)
    }
  }
}