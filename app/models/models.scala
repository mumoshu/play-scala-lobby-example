package models

import javax.persistence.Entity
import play.db.anorm._
import play.db.anorm.SqlParser._
import play.db.anorm.defaults._
import play.libs.{Crypto, Codec}
import play.utils.Scala.MayErr
import play.mvc.Router
import play.db.DB

/**
 * The user.
 * Passwords are not kept as plain text but hashed.
 * Also, emails are encrypted.
 */
case class User(
  id: Pk[Long],
  name: String,
  password: String,
  email: String,
  iconPath: String
) {
  def iconUrl(): String = {
    Router.reverse(play.Play.getVirtualFile("public" + iconPath))
  }
}

object User extends Magic[User] {
  def findByAccessToken(accessToken: String): Option[User] = {
    SQL("""
      select * from oauth2session s
      join user u
      on s.userId = u.id
      where s.accessToken = {accessToken}
    """).on("accessToken" -> accessToken)
    .as(User ?)
  }

  def findById(id: Long): Option[User] = find("id={id}").on("id" -> id).first()

  /**
   * Create and save a User.
   */
  def join(name: String, password: String, email: String, icon: String): User = create(
    User(NotAssigned, name, Crypto.passwordHash(password), Crypto.encryptAES(email), icon)
  ).get

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

case class Avatar(id: Pk[Long], name: String, iconPath: String)

object Avatar extends Magic[Avatar]

case class Game(id: Pk[Long], title: String, appUrl: String) {
  def formatAppUrl(host: String, port: Int, channel: Long, token: String): String = {
    "%s?host=%s&port=%d&channel=%d&token=%s".format(appUrl, host, port, channel, token)
  }
  def lobbies() = Lobby.find("gameId = {gameId}").on("gameId" -> id()).as(Lobby *)
}

object Game extends Magic[Game]

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
