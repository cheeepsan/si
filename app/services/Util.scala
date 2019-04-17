package services

import java.io.{File, FileInputStream, FileOutputStream, IOException}
import java.nio.charset.StandardCharsets
import java.nio.file.{DirectoryStream, Path}
import java.util.zip.{ZipEntry, ZipInputStream}

import models.si.{SiRound, SiTheme}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

object Util {

  def unzip(pathToUnzip: String, destToUnzip: File): DirectoryStream[Path] = {


    if (!destToUnzip.exists()) {
      destToUnzip.mkdir()
    }

    val buffer = new Array[Byte](1024)

    val zis = new ZipInputStream(new FileInputStream(pathToUnzip))
    var zipEntry = zis.getNextEntry
    while (zipEntry != null) {
      val newFile = this.newFile(destToUnzip, zipEntry)
      val fos: FileOutputStream = new FileOutputStream(newFile)
      var len: Int = zis.read(buffer)
      while (len > 0) {
        fos.write(buffer, 0, len)
        len = zis.read(buffer)
      }
      fos.close()
      zipEntry = zis.getNextEntry
    }
    zis.closeEntry()
    zis.close()
    java.nio.file.Files.newDirectoryStream(destToUnzip.toPath)
  }


  @throws[IOException]
  def newFile(destinationDir: File, zipEntry: ZipEntry): File = {
    val decodedName = java.net.URLDecoder.decode(zipEntry.getName, StandardCharsets.UTF_8.name())
    val destFile = new File(destinationDir, decodedName)
    val parentFolder = destFile.getParentFile
    if (!parentFolder.exists()) parentFolder.mkdirs()

    destFile
  }

}

object JsonUtils {
  //    def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
  //      def reads(json: JsValue): JsResult[E#Value] = json match {
  //        case JsString(s) => {
  //          try {
  //            JsSuccess(enum.withName(s))
  //          } catch {
  //            case _: NoSuchElementException => JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
  //          }
  //        }
  //        case _ => JsError("String value expected")
  //      }
  //    }
  //
  //    implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
  //      def writes(v: E#Value): JsValue = JsString(v.toString)
  //    }
  //
  //    implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
  //      Format(EnumUtils.enumReads(enum), EnumUtils.enumWrites)
  //    }

//  implicit val mapReads: Reads[Map[SiTheme, List[SiRound]]] = new Reads[Map[SiTheme, List[SiRound]]] {
//    override def reads(json: JsValue): JsResult[Map[SiTheme, List[SiRound]]] = JsSuccess(json.as[Map[String, Boolean]].map {
//      case (k, v) =>
//        k.asInstanceOf[SiTheme] -> v.asInstanceOf[List[SiRound]]
//    })
//  }
//
//  implicit val mapWrites: Writes[Map[SiTheme, List[SiRound]]] = new Writes[Map[SiTheme, List[SiRound]]] {
//    def writes(map: Map[SiTheme, List[SiRound]]): JsValue =
//      Json.obj(
//        map.map {
//          case (s, o) =>
//            val ret = s.name -> Json.toJsFieldJsValueWrapper(o)
//            ret
//        }.toSeq: _*)
//  }
//
//  implicit val mapFormat: Format[Map[SiTheme, List[SiRound]]] = Format(mapReads, mapWrites)
}