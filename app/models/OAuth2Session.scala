package models

import play.db.anorm._
import play.db.anorm.defaults.Magic
import java.math.BigInteger
import java.security.SecureRandom

/**
 * OAuth2 session persists in the database.
 */
case class OAuth2Session(
  val id: Pk[Long],
  val userId: Long,
  val accessToken: String
)

object OAuth2Session extends Magic[OAuth2Session] {
  def continueOrCreate(session: OAuth2Session): OAuth2Session = {
    val success = SQL("delete from oauth2session where userId = {userId}").on("userId" -> session.userId).execute()
    OAuth2Session.create(session).get
  }

  def findByUser(user: User): Option[OAuth2Session] =
    find("userId = {userId}").on("userId" -> user.id()).as(OAuth2Session ?)

  def findOrCreateForUser(user: User): Option[OAuth2Session] = {
    val oauth: OAuth2Session = findByUser(user).getOrElse(create(OAuth2Session(NotAssigned, user.id(), generateAccessToken)).get)
    Option(oauth)
  }

  def generateAccessToken() = new BigInteger(100, new SecureRandom()).toString(36)

}
