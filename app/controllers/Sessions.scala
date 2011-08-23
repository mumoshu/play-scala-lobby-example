package controllers

import play.mvc._
import models.User
import scala.Some
import play.cache.Cache
import play.data.validation.{Error, Validation}
import play.Logger

/**
 * Cookieセッションの作成に関するController
 */
object Sessions extends Controller {
  /**
   * メアドとパスワードで認証して、ログインするアクション
   */
  def create() = {
    val email = params.get("email")
    val password = params.get("password")
    val passwordConfirmation = params.get("password_confirmation")

    Validation.required("email", email)
    Validation.required("password", password)
    Validation.email("email", email)

    if (Validation.hasErrors) {
      Application.login()
    } else {
      val userOption = User.findByEmailAndPassword(email, password)
      userOption match {
        case Some(user) => {
          Cache.set(session.getId + "-user", user, "1h")
          Action(Rooms.index())
        }
        case None => {
          Logger.info("not logged in ")
          Validation.addError("emailOrPassword", "validation.valid")
//          Validation.keep()
          Application.login()
        }
      }
    }
  }

}