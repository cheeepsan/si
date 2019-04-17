package services

import java.io.File

import models.si._

import scala.xml.{Elem, Node, NodeSeq, XML}

class SiqParser {

  val contentXMLName = "content.xml"

  def process(dir: File): SiPackage = {
    val contentXML = XML.loadFile(dir.getAbsolutePath + "\\" + contentXMLName)

    val xmlPackage = contentXML \\ "package"
    val rounds: List[SiRound] = this.createRounds(xmlPackage)
    val pack = this.createPackage(xmlPackage, rounds)
    pack
  }

  def createPackage(xml: NodeSeq, rounds: List[SiRound]): SiPackage = {
    val name = xml \@ "name"
    val version = xml \@ "version"
    val date = xml \@ "date"
    val authors = (xml \\ "author").map(_.text).toList
    new SiPackage(name, version, date, authors, rounds)
  }

  def createRounds(xml: NodeSeq): List[SiRound] = {
    val rounds: List[SiRound] = (xml \\ "round").map {
      roundNode =>
        val roundName = roundNode \@ "name"
        val questions: Map[String, List[SiQuestion]] = (roundNode \\ "theme").flatMap {
          themeNode =>
            val theme: String = this.createTheme(themeNode)
            val questionList = this.createQuestionList(themeNode \\ "question", theme)

            Map(theme -> questionList)
        }.toMap
        new SiRound(roundName, questions)
    }.toList
    rounds
  }

  def createTheme(themeNode: Node): String = {
    val themeName = themeNode \@ "name"
    themeName
  }

  def createQuestionList(questionNode: NodeSeq, theme: String): List[SiQuestion] = {
    questionNode.map {
      questionNode =>
        this.createQuestion(questionNode, theme)
    }.toList
  }

  def createQuestion(question: NodeSeq, theme: String): SiQuestion = {
    val price: Int = (question \@ "price").toInt
    val answer: List[String] = (question \ "right" \ "answer").map(_.text).toList

    val typeNode = (question \ "type").headOption
    val qType: SiQuestionType.Value = typeNode match {
      case Some(t) => val typeName = t \@ "name"
        SiQuestion.getQuestionType(typeName)
      case None => SiQuestionType.SI_QUESTION
    }

    val scenario = (question \\ "atom").head
    val text = scenario.text
    val scenarioType = SiQuestion.getQuestionScenario(scenario \@ "type")
    SiQuestion.constructQuestion(price, text, answer, qType, scenarioType, theme, typeNode)
  }


}
