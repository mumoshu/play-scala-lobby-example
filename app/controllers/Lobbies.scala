package controllers

import play.mvc._
import models._
import play.data.validation.Validation
import play.db.anorm._
import play.db.anorm.defaults._

/**
 * ゲーム1つにつき１つだけ存在する「ロビー」
 */
object Lobbies extends Controller with Secure with ReadableResource[Lobby] {
  import views.Lobbies._

  val resourceManifest = manifest[Lobby]
  override val plural = Option("lobbies")

  def newLobby() = {
    html.newLobby(Game.find().as(Game *))
  }

  def index() = {
    val lobbies = Lobby.find().as(Lobby *)
    html.index(user, lobbies)
  }

  def create() = {
    val lobbyTitle = params.get("lobby.title")
    val gameId = params.get("game.id")

    Validation.required("lobbyTitle", lobbyTitle)
    Validation.required("gameId", gameId)

    val lobby = Lobby.create(Lobby(NotAssigned, lobbyTitle, gameId.toLong))
    show(lobby.id())
  }

  def show(lobbyId: Long) = {
    val rooms = Room.all
    val lobby = Lobby.find("id={lobbyId}").on("lobbyId" -> lobbyId).as(Lobby)
    val room = Room.forLobby(lobby)

    html.show(user, rooms, lobby, room)
  }
}
