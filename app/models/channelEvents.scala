package models

abstract class Event(val `type`: String)
case class Join(val user: User) extends Event("join")
case class Leave(val user: User) extends Event("leave")
case class Say(val user: User, what: String) extends Event("message")
case class Play(val game: Game) extends Event("play")
case class Broadcast(from: User, message: String) extends Event("broadcast")
