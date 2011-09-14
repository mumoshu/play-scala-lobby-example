package controllers

import play.mvc.{Before, Controller}
import models.User
import play.cache.Cache
import scala.Some
import play.Logger

trait Secure {
  self: Controller =>

  var user: User = null

  val needsSessionOnlyFor: Set[String] = Set.empty

  /**
   * Cookieセッションが存在してログイン状態であることを強制する.
   * ログインしていない場合はログインページへ転送する。
   */
  @Before(unless = Array("Lobbies.getResource", "Lobbies.getResources"))
  def ensureLogin(): play.mvc.results.Result = {
    if (!needsSessionOnlyFor.isEmpty && !needsSessionOnlyFor(request.actionMethod)) {
      return Continue
    }

    val userOption: Option[User] = Cache.get(session.getId + "-user")
    userOption match {
      case Some(user) => {
        this.user = user
        Logger.info("Logged in as: %s", user)
        Continue
      }
      case None => {
        flash.put("message", "ログインしてください")
        Action(Application.login)
      }
    }
  }

}