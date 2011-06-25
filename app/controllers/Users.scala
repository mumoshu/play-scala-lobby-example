package controllers

import models.User
import play.cache.Cache
import play.mvc.Scope.Session
import play.data.validation.Validation
import play.mvc._

// This does not compile with the exception below.
// Oops: UnexpectedException
// An unexpected error occured caused by exception UnexpectedException:
// While applying class play.classloading.enhancers.LocalvariablesNamesEnhancer
//
//object UsersAPI extends Controller with ReadableResource[Long, User] {
//  protected val manifestForResourceClass = manifest[User]
//  protected val parser = User
//}

object Users extends Controller with ReadableUserResource {
  import views.Users.html

  def join() = html.join()

  def create() = {
    val name = params.get("name")
    val password = params.get("password")
    val email = params.get("email")

    Validation.required("name", name).message("Name is required.")
    Validation.required("email", email)
    Validation.required("password", password)
    Validation.email("email", email).message("Malformed email address.")

    if (Validation.hasErrors) {
      join()
    } else {
      val user = User.join(name, password, email)
      Cache.add(session.getId + "-user", user)
      created(user)
    }
  }

  def created(user: User) = html.created(user)
}
