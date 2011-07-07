package controllers

import play._
import libs.F.ArchivedEventStream
import mvc._
import mvc.Http.WebSocketEvent
import models._
import play.libs.F.Promise
import java.util.HashMap

object Rooms extends Controller with Secure {
  import views.Rooms._

  def index() = {
    html.index(Room.all, user)
  }

  def create() = {
    val title: String = params.get("title")
    val username: String = params.get("username")
//    html.show(title, username)
    val url = {
      val params = new HashMap[String, String]
      params.put("title", title)
      params.put("username", username)
      Router.getFullUrl("Rooms.show", params.asInstanceOf[java.util.Map[String, Object]])
    }
    Logger.info("Redirecting to: %s", url)
    Redirect(url)
  }

  def show(title: String, username: String) = {
    html.show(title, username)
  }
}
