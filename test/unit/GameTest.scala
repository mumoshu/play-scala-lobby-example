package test

import _root_.test.FixturesFromDataYml
import models._
import play._
import libs.Crypto
import play.test._

import org.scalatest._
import matchers._
import play.db.anorm._
import play.db.anorm.defaults._
import play.db.anorm.SqlParser._

class GameTest extends UnitFlatSpec with ShouldMatchers with FixturesFromDataYml {

  it should "create an instance of Game" in {
    val title = "title"
    val appUrl = "tetris://"
    val mayErr = Game.create(Game(NotAssigned, title, appUrl))
    val game = mayErr.get

    game.title should be (title)
    game.appUrl should be (appUrl)
  }

  it should "retrieve formatted appUrl" in {
    val appUrl = "tetris://"
    val game = Game(NotAssigned, "title", appUrl)

    val host = "localhost"
    val port = 1234
    val channel = 123
    val token = "authenticationToken"
    val url : String = game.formatAppUrl(host, port, channel, token)

    url should be ("tetris://?host=" + host + "&port=" + port + "&channel=" + channel + "&token=" + token)
  }
}