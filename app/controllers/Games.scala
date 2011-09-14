package controllers

import play.mvc._
import models._
import play.data.validation.Validation
import play.db.anorm._
import play.db.anorm.defaults._
import views.Games._

/**
 * @author KUOKA Yusuke
 * ゲームタイトルに関するWebページやAPIのコントローラ
 */
object Games extends Controller with Secure {
  def index() = html.index(user, Game.find().list().toList)
  def show(id : Long) = html.show(
    user,
    Game.find("id = {id}").on("id" -> id).single(),
    Lobby.find("gameId = {gameId}").on("gameId" -> id).list().toList
  )
  def add() = html.add(user)
  def create() = {
    import collection.JavaConversions._

    val title = params.get("title")
    val appUrl = params.get("appUrl")

    /**
     * <code>Game.create(Game)</code>
     * でエラーが発生していた場合
     * {@link MayErr#get)}
     * は{@link RuntimeException}を投げる。
     */
    val game = Game.create(Game(NotAssigned, title, appUrl)).get
    val lobby = Lobby.create(Lobby(NotAssigned, "Lobby #1", game.id())).get
//    val data: java.util.Map[String, Object] = asJavaMap(Map("gameId" -> new java.lang.Long(game.id())))
//    val url = Router.reverse("Games.show", data).url
//    Redirect(url)
    Action(show(game.id()))
  }
}