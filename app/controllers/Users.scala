package controllers

import models.{User, Avatar}
import play.cache.Cache
import play.mvc.Scope.Session
import play.data.validation.Validation
import play.mvc._
import java.lang.Long
import net.liftweb.json.FieldSerializer
import play.db.anorm._
import play.db.anorm.defaults._
import play.Play

// This does not compile with the exception below.
// Oops: UnexpectedException
// An unexpected error occured caused by exception UnexpectedException:
// While applying class play.classloading.enhancers.LocalvariablesNamesEnhancer
//
//object UsersAPI extends Controller with ReadableResource[Long, User] {
//  protected val manifestForResourceClass = manifest[User]
//  protected val resourceParser = User
//}
object UsersAPI extends Controller with ReadableResource[User] {
  // You get a NullPointerException at
  //   play.db.anorm.Analyser$class.$init$(Anorm.scala:600)
  // when you write the below as implicit, like 'implicit val resourceManifest = manifest[User]'
  val resourceManifest = manifest[User]
  // You need to explicitly provide return type or you will get an exception.
//  val resourceParser: Magic[User] = User
}

object Users extends Controller with ReadableUserResource {
  import views.Users.html

  def icons = Avatar.find().as(Avatar *).map(_.name)

  def join() = html.join(icons)

  /**
   * 新しいユーザを登録してログインするアクション
   */
  def create() = {
    val name = params.get("name")
    val password = params.get("password")
    val email = params.get("email")
    val icon = params.get("icon")

    Validation.required("name", name).message("Name is required.")
    Validation.required("email", email)
    Validation.required("password", password)
    Validation.required("icon", icon)
    Validation.email("email", email).message("Malformed email address.")

    val virtualFilePath = "public/images/avatars/" + icon + ".gif"
    val virtualFile = Play.getVirtualFile(virtualFilePath)

    if (virtualFile == null || Router.reverse(virtualFile) == null) {
      Validation.addError("icon", "Icon not found: %s", virtualFilePath)
    }

    if (Validation.hasErrors) {
      join()
    } else {
      val user = User.join(name, password, email, "images/avatars/" + icon + ".gif")
      Cache.add(session.getId + "-user", user)
      created(user)
    }
  }

  def created(user: User) = html.created(user)
}
