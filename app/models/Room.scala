package models

import play.libs.F.ArchivedEventStream

case class Room(id: Long, title: String, gameId: Long) {
  val events = new ArchivedEventStream[Event](100)
  val users = collection.mutable.Set[User]()

  private def publish(event: Event) {
    events.publish(event)
  }

  def join(user: User) {
    users.add(user)
    publish(Join(user))
  }

  def leave(user: User) {
    publish(Leave(user))
  }

  def say(user: User, what: String) {
    publish(Say(user, what))
  }

  def play(game: Game) {
    publish(Play(game))
  }

  def broadcast(user: User, message: String) {
    publish(Broadcast(user, message))
  }
}

object Room {
  val rooms = collection.mutable.Set[Room]()
  var lastId: Long = 0

  def create(title: String, gameId: Long) = {
    val room = Room(lastId, title, gameId)
    lastId += 1
    rooms.add(room)
    room
  }
  def findByTitle(title: String): Option[Room] = rooms.find { _.title == title }
  def findById(id: Long): Option[Room] = rooms.find { _.id == id }
  def all = rooms.toList
  def forLobby(lobby: Lobby): Room = findByTitle(lobby.title).getOrElse((create(lobby.title, lobby.gameId)))
}