package controllers

import play._
import libs.F.ArchivedEventStream
import mvc._
import mvc.Http.WebSocketEvent
import models._
import play.libs.F.Promise
import sbt.SessionSettings

object Application extends Controller {
    
    import views.Application._
    
    def index = {
        html.index("Your Scala application is ready!")
    }

  def login() = {
    html.login()
  }
    
}



