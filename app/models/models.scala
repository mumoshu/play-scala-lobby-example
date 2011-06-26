package models

import javax.persistence.Entity
import play.db.anorm._
import play.db.anorm.SqlParser._
import play.db.anorm.defaults._
import play.libs.{Crypto, Codec}
import play.utils.Scala.MayErr

/**
 * The user.
 * Passwords are not kept as plain text but hashed.
 * Also, emails are encrypted.
 */
case class User(
  id: Pk[Long],
  name: String,
  password: String,
  email: String
)

object User extends Magic[User] {
  /**
   * Create and save a User.
   */
  def join(name: String, password: String, email: String): User = create(
    User(NotAssigned, name, Crypto.passwordHash(password), Crypto.encryptAES(email))
  )

  /**
   * Find the user with the specified email and password.
   */
  def findByEmailAndPassword(email: String, password: String): Option[User] =
    find("email={email} and password={password}").on(
      "email" -> Crypto.encryptAES(email),
      "password" -> Crypto.passwordHash(password)
    ).first()

  def findByIdWithAchievements(id: Long) =
    SQL("""
      select * from User u
      join UserAchievement ua on ua.userId = u.id
      left join Achievement a on a.id = ua.achievementId
      where u.id = {userId}
    """)
      .on("userId" -> id)
      .as(User ~< (Achievement*) ?)
}

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
  var score: Int,
  var imageUrl: String
)

object Achievement extends Magic[Achievement]

case class UserAchievement(
  id: Pk[Long],
  userId: Long,
  achievementId: Long
)

object UserAchievement extends Magic[UserAchievement]

case class WebSocketAuthorization(
  userId: Pk[Long],
  sessionId: String
)

object WebSocketAuthorization extends Magic[WebSocketAuthorization] {
  def findByUserId(userId: Long) = find("userId={userId}").on("userId" -> userId).first()
  def findBySessionId(sessionId: String) = find("sessionId={sessionId}").on("sessionId" -> sessionId).first()
}

abstract class Event(val `type`: String)
case class Join(val user: User) extends Event("join")
case class Leave(val user: User) extends Event("leave")
case class Say(val user: User, what: String) extends Event("message")
case class Play(val game: Game) extends Event("play")
