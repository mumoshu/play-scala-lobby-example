package models

import java.lang.Long
import play.db.anorm._
import play.db.anorm.defaults.Magic

/**
 * OAuth2 session persists in the database.
 */
case class OAuth2Session(
  val id: Pk[Long],
  val userId: Long,
  val accessToken: String
)

object OAuth2Session extends Magic[OAuth2Session]
