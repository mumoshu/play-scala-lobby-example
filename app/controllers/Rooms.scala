package controllers

import play._
import data.validation.Validation
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
    val gameId = params.get("gameId")

    Validation.required("gameId", gameId)

    if (Validation.hasErrors) {
      Action(index())
    }

    val room = Room.create(title, (if (gameId == null) "1" else gameId).toLong)
    val url = {
      val params = new HashMap[String, String]
      params.put("roomId", room.id.toString)
      params.put("userId", user.id.toString)
      Router.getFullUrl("Rooms.show", params.asInstanceOf[java.util.Map[String, Object]])
    }
    Logger.info("Redirecting to: %s", url)
    Redirect(url)
  }

  def show(roomId: Long, userId: Long) = {
    val room: Room = Room.findById(roomId).get
    val user: User = User.findById(userId).get
    val gameIdString: String = Option(params.get("gameId")).getOrElse("1")
    val gameId: Long = gameIdString.toLong
    val game: Game = Game.find("id = {id}").on("id" -> gameId).as(Game)
    html.show(room, user, game)
  }
}
