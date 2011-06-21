package models

import play.libs.F.ArchivedEventStream

case class Room(title: String) {
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
}

object Room {
  val rooms = collection.mutable.Set[Room]()

  def create(title: String) = {
    val room = Room(title)
    rooms.add(room)
    room
  }
  def findByTitle(title: String): Option[Room] = rooms.find { _.title == title }
  def all = rooms
}