package models

import play.db.anorm._
import play.db.anorm.defaults._

case class Lobby(id: Pk[Long], title: String)

object Lobby extends Magic[Lobby]
