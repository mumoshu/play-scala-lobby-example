package controllers

import play._
import libs.F.ArchivedEventStream
import mvc._
import mvc.Http.WebSocketEvent
import models._
import play.libs.F.Promise

object Rooms extends Controller {
  import views.Rooms._

  def index(username: String) = {
    html.index(Room.all, username)
  }

  def create() = {
    val title = params.get("title")
    val username = params.get("username")
    html.show(title, username)
  }

  def show(title: String, username: String) = {
    html.show(title, username)
  }
}
