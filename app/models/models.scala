package models

case class User(name: String)

case class Game(title: String) {
  // WebSocketの接続先アドレス
  // 例えばこの例ではテトリスのID=123の部屋
  def url: String = {
    "/games/tetris/123"
  }
}

abstract class Event(val `type`: String)
case class Join(val user: User) extends Event("join")
case class Leave(val user: User) extends Event("leave")
case class Say(val user: User, what: String) extends Event("message")
case class Play(val game: Game) extends Event("play")

