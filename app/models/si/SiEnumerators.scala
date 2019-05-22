package models.si

import play.api.libs.json.{Reads, Writes}

object SiQuestionType extends Enumeration {
  type SiQuestionType = Value
  val SI_QUESTION, SI_CAT, SI_AUCTION = Value
  implicit val siScenarioReads: Reads[SiQuestionType.Value] = Reads.enumNameReads(SiQuestionType)
  implicit val siScenarioWrites: Writes[SiQuestionType.Value] = Writes.enumNameWrites
  //  implicit val siQuestionTypeFormat: Format[SiQuestionType.Value] = EnumUtils.enumFormat(SiQuestionType)
  //  implicit val siQuestionTypeWrites: Writes[SiQuestionType.Value] = Writes.enumNameWrites
}

object SiScenario extends Enumeration {
  val SI_TEXT, SI_IMAGE, SI_AUDIO, SI_VIDEO = Value

  implicit val siScenarioReads: Reads[SiScenario.Value] = Reads.enumNameReads(SiScenario)
  implicit val siScenarioWrites: Writes[SiScenario.Value] = Writes.enumNameWrites
}