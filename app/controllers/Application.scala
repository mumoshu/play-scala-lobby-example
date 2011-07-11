package controllers

import play._
import cache.Cache
import libs.F.ArchivedEventStream
import mvc._
import mvc.Http.WebSocketEvent
import models._
import play.libs.F.Promise
import sbt.SessionSettings
import test._

object Application extends Controller {
    
    import views.Application._
    
    def index = {
        html.index("Your Scala application is ready!")
    }

  def login() = {
    html.login()
  }

  def logout() = {
    Cache.delete(session.getId + "-user")
    Redirect("login")
  }

  def loadFixtures = {
    Fixtures.deleteDatabase()
    Yaml[List[Any]]("initial-data.yml").foreach {
      case user:User => User.create(user)
      case avatar:Avatar => Avatar.create(avatar)
      case lobby:Lobby => Lobby.create(lobby)
      case _ => ()
    }
  }
    
}



