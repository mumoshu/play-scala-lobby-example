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

trait OAuth {
  self: Controller =>

  var user: Option[User] = None

  @Before(unless = Array("OAuth2.token"))
  def ensureAuthorized() = {
    import controllers.UsingJson._

    val authorization = Option(request.headers.get("authorization"))
    val regex = "OAuth (.+)".r

    user = authorization.map(_.value) match {
      case Some(regex(accessToken)) =>
        User.findByAccessToken(accessToken)
      case _ =>
        None
    }

    play.Logger.info("url: %s", request.url)
    play.Logger.info("Authorization: %s", authorization)
    play.Logger.info("User: %s", user)

    if (user isDefined)
      Continue
    else
      Error(400, write(oauth.InvalidRequest()))
  }
}

object OAuth2 extends Controller with OAuth {
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
          val session = OAuth2Session.continueOrCreate(OAuth2Session(NotAssigned, userId, accessToken))
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
}