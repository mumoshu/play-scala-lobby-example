package controllers

import play.Logger
import play.data.validation.Validation
import play.db.anorm._
import models.{OAuth2Session, Achievement, UserAchievement, User}
import java.math.BigInteger
import java.security.SecureRandom
import response.{TokenResponse, UserAchievementsResponse}
import net.liftweb.json._
import net.liftweb.json.Serialization.write
import play.mvc.{Before, Controller}
import java.lang.reflect.Constructor
import play.classloading.enhancers.LocalvariablesNamesEnhancer

/**
 * See http://groups.google.com/group/liftweb/browse_thread/thread/b6d5a00605dd9e36
 */
object PlayParameterNameReader extends ParameterNameReader {
  import collection.JavaConversions._
  def lookupParameterNames(constructor: Constructor[_]): Traversable[String] =
    LocalvariablesNamesEnhancer.lookupParameterNames(constructor)
}

object UsingJson {

  implicit val jsonFormats = new Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = ShortTypeHints(List(classOf[Achievement]))
    override val parameterNameReader: ParameterNameReader = PlayParameterNameReader
  } +
    FieldSerializer[AnyRef]() +
    new controllers.PkSerializer
}

object OAuth2 extends Controller {
  import controllers.UsingJson._

  @Before
  def fixRequestInJson = {
    // A tiny hack to send the response in JSON.
    request.format = "json"
    Continue
  }

  def token(username: String, password: String) = {
    val grantType = params.get("grant_type")

    Validation.required("username", username)
    Validation.required("password", password)
    Validation.required("grantType", grantType)

    if (Validation.hasErrors) {
      val description = "Validation error: %s".format(Validation.errors())
      val response = TokenResponse(Some("invalid_request"), Some(description), None)
      Error(400, write(response))
    } else {
      val userOption = User.find("email = {username}").on("username" -> username).as(User ?)
      userOption match {
        case Some(user) => {
          val userId = user.id()
          val accessToken = new BigInteger(100, new SecureRandom()).toString(36)
          val session = OAuth2Session.create(OAuth2Session(NotAssigned, userId, accessToken))
          val response = TokenResponse(None, None, Option(accessToken))
          Json(write(response))
        }
        case _ => {
          val response = TokenResponse(Option("invalid_grant"), Option("User not found."), None)
          Error(400, write(response))
        }
      }
    }
  }

  def find(userId: Long) = {
    Validation.required("userId", userId)

    if (Validation.hasErrors) {
      val errorDescription = "Validation error: %s".format(Validation.errors())
      Logger.error(errorDescription)
      val response = UserAchievementsResponse(
        Some("invalid_request"),
        Some(errorDescription),
        Nil
      )
      Error(400, write(response))
    } else {
      import play.db.anorm.SqlParser._
      val Some(user~achievements) = SQL("""
        select * from UserAchievement ua
        left join Achievement a on ua.achievementId = a.id
        where ua.userId = {userId}
      """).on("userId" -> userId)
      .as(UserAchievement ~< (Achievement *) ?)
      val response = UserAchievementsResponse(None, None, achievements)
      Json(write(response))
    }
  }

  def create(userId: Long, achievementId: Long) = {
    Validation.required("userId", userId)
    Validation.required("achievementId", achievementId)

    if (Validation.hasErrors) {
      Logger.error("Validation error: %s", Validation.errors())
      Error(400, Validation.errors().toString)
    } else {
      val userOption = User.find("id = {id}").on("id" -> userId).as(User ?)
      val achievementOption = Achievement.find("id = {id}").on("id" -> achievementId).as(Achievement ?)
      if (userOption.isEmpty) {
        Error(400, "User for id " + userId + " is not found.")
      } else if (achievementOption.isEmpty) {
        Error(400, "Achievement for id " + achievementId + " is not found.")
      } else {
        val userAchievement = UserAchievement.create(UserAchievement(NotAssigned, userId, achievementId))
        Ok
      }
    }
  }
}