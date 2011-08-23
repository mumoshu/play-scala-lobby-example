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
    val gameIdString = Option(params.get("gameId")).getOrElse("1")
    val gameId = gameIdString.toLong
    val game: Game = Game.find("id = {id}").on("id" -> gameId).as(Game)
    val maybeOAuth: Option[OAuth2Session] = OAuth2Session.findOrCreateForUser(user)
    val maybeRunAppUrl: Option[String] = maybeOAuth.map(oauth => game.formatAppUrl(request.domain, request.port.intValue, roomId, oauth.accessToken))

    maybeRunAppUrl match {
      case Some(runAppUrl) => html.show(room, user, game, runAppUrl)
      case None => Error("ゲーム起動URLが生成できませんでした")
    }
  }
}
