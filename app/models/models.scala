package models

import javax.persistence.Entity
import play.db.anorm._
import play.db.anorm.SqlParser._
import play.db.anorm.defaults._

case class User(name: String)

case class Game(title: String) {
  // WebSocketの接続先アドレス
  // 例えばこの例ではテトリスのID=123の部屋
  def url: String = {
    "/games/tetris/123"
  }
}

//Annotating Achievement with @Entity causes the following exception on runtime.
//A JPA error occurred (Unable to build EntityManagerFactory): No identifier specified for entity: models.Achievement
case class Achievement(
  id: Pk[Long],
  var title: String,
  var description: String,
  var imageUrl: String
)

object Achievement extends Magic[Achievement]

abstract class Event(val `type`: String)
case class Join(val user: User) extends Event("join")
case class Leave(val user: User) extends Event("leave")
case class Say(val user: User, what: String) extends Event("message")
case class Play(val game: Game) extends Event("play")

