package controllers.common

import javax.inject._
import models.common.Global
import models.common.siUser.{LoginObject, User}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{ignored, mapping}
import play.api.mvc._
import services.ClientServerInit
import play.api.data.Form
import play.api.data.Forms._
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */

class UserController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
  val loginForm: Form[LoginObject] = Form(
    mapping (
      "username" -> text,
      "address" -> text
  )(LoginObject.apply)(LoginObject.unapply))

  private val formSubmitUrl = routes.UserController.processLoginAttempt


  def showLoginForm = Action {  implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.common.user.login(loginForm, formSubmitUrl))
  }

  def processLoginAttempt = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[LoginObject] =>
      BadRequest(views.html.common.user.login(formWithErrors, formSubmitUrl))
    }

    val successFunction = { user: LoginObject =>
      Redirect(s"/client/index?addressString=${user.address}")
        .flashing("info" -> "You are logged in.")
        .withSession(Global.SESSION_USERNAME_KEY -> user.username)
    }
    val formValidationResult: Form[LoginObject] = loginForm.bindFromRequest
    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }

}
