package controllers

import play.mvc._
import models.User
import play.cache.Cache
import play.mvc.Scope.Session
import play.data.validation.Validation

object Users extends Controller with UsersAPI {
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
