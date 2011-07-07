package controllers

import play.mvc._
import models._
import play.data.validation.Validation
import play.db.anorm._
import play.db.anorm.defaults._

/**
 * ゲーム1つにつき１つだけ存在する「ロビー」
 */
object Lobbies extends Controller with Secure {
  import views.Lobbies._

  def newLobby() = {
    html.newLobby()
  }

  def create() = {
    val lobbyTitle = params.get("lobby.title")

    Validation.required("lobbyTitle", lobbyTitle)
    val lobby = Lobby.create(Lobby(NotAssigned, lobbyTitle))
    show(lobby.id())
  }

  def show(lobbyId: Long) = {
    val rooms = Room.all
    val lobby = Lobby.find("id={lobbyId}").on("lobbyId" -> lobbyId).as(Lobby)

    html.show(user, rooms, lobby)
  }
}
