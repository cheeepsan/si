package models.si


import play.api.libs.json._
import play.api.libs.functional.syntax._


import scala.xml.Node

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

case class SiQuestion(price: Int,
                      text: String,
                      theme: String,
                      answer: List[String],
                      qType: SiQuestionType.Value,
                      scenario: SiScenario.Value) extends SiObject {


}

object SiQuestion {

  implicit val siQuestionReads: Reads[SiQuestion] = (
    (JsPath \ "price").read[Int] and
      (JsPath \ "text").read[String] and
      (JsPath \ "theme").read[String] and
      (JsPath \ "answer").read[List[String]] and
      (JsPath \ "qType").read[SiQuestionType.Value] and
      (JsPath \ "scenario").read[SiScenario.Value]
    )(SiQuestion.apply _)

  implicit val siQuestionWrites: Writes[SiQuestion] = (
    (JsPath \ "price").write[Int] and
      (JsPath \ "text").write[String] and
      (JsPath \ "theme").write[String] and
      (JsPath \ "answer").write[List[String]] and
      (JsPath \ "qType").write[SiQuestionType.Value] and
      (JsPath \ "scenario").write[SiScenario.Value]
    )(unlift(SiQuestion.unapply))

  def getQuestionType(typeName: String): SiQuestionType.Value = typeName match {
    case "cat" => SiQuestionType.SI_CAT
    case "auction" => SiQuestionType.SI_AUCTION
    case _ => SiQuestionType.SI_QUESTION
  }

  def getQuestionScenario(t: String): SiScenario.Value = t match {
    case "image" => SiScenario.SI_IMAGE
    case "audio" => SiScenario.SI_AUDIO
    case "voice" => SiScenario.SI_AUDIO
    case "video" => SiScenario.SI_VIDEO
    case _ => SiScenario.SI_TEXT
  }

  def apply(price: Int,
            text: String,
            theme: String,
            answer: List[String],
            qType: SiQuestionType.Value,
            scenario: SiScenario.Value): SiQuestion = new SiQuestion(price, text, theme, answer, qType, scenario)

  def constructQuestion(price: Int,
            text: String,
            answer: List[String],
            qType: SiQuestionType.Value,
            scenarioType: SiScenario.Value,
            theme: String,
            typeNode: Option[Node]): SiQuestion = {

    val themeAndPrice = typeNode match {
      case Some(n) => this.xmlFindPrice(qType, price, theme, n)
      case None => (theme, price)
    }

    apply(themeAndPrice._2, text, themeAndPrice._1, answer, qType, scenarioType)
  }

  def xmlFindPrice(qType: SiQuestionType.Value, price: Int, theme: String, node: Node): (String, Int) = qType match {
    case SiQuestionType.SI_AUCTION => (node \\ "param").headOption match {
      case Some(x) => (x \@ "theme", 0)
      case None => ("err", 0)
    }
    case SiQuestionType.SI_CAT =>
      var theme: String = ""
      var cost: Int = 0 //we just imply
      (node \\ "param").foreach {
        case n =>
          n \@ "name" match {
            case "theme" =>
              theme = n.text
            case "cost" =>
              cost = n.text.toInt
          }
      }
      (theme, cost)
    case SiQuestionType.SI_QUESTION => (theme, price)
  }
}