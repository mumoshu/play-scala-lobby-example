import controllers.PkSerializer
import java.io.File
import models.Achievement
import net.liftweb.json.DefaultFormats
import org.scalatest.matchers.ShouldMatchers
import play.mvc.Http.{Header, Request}
import play.test.{Yaml, FunctionalFlatSpec}
import play.test.{Fixtures, UnitFlatSpec}
import models._
import collection.JavaConversions._
import net.liftweb.json._
import net.liftweb.json.Serialization.read
import response.TokenResponse
import response.UserAchievementsResponse
import play.Logger

import play.db.anorm._
import play.db.anorm.defaults._

trait RestTestsHelpers {
  def requestWithAuthorization(header: Header) = {
    val request = new Request()
    request.headers.put("authorization", header)
    request
  }
}

trait RestTestsConstants {
  val userId = 1L
  val accessToken = "accessToken1"
  val oauth2Header = new Header(
    "Authorization",
    "OAuth " + accessToken
  )
  val invalidOauth2Header = new Header(
    "Authorization",
    "OAuth invalidAccessToken"
  )
  val emptyFiles = Map.empty[String, File]
  val achievementId = "1"
  val userAchievementsUrl = "/api/users/1/achievements"
}

class RestTests extends FunctionalFlatSpec with ShouldMatchers with RestTestsHelpers with RestTestsConstants {
  def deleteDatabaseAndLoadFixtures {
    Fixtures.deleteDatabase()
    Yaml[List[Any]]("data.yml").foreach {
      _ match {
        case u: User => User.create(u)
        case a: Achievement => Achievement.create(a)
        case ua: UserAchievement => UserAchievement.create(ua)
        case os: OAuth2Session => OAuth2Session.create(os)
        case _ => ()
      }
    }
  }

  lazy val user = User.find("id = {id}").on("id" -> userId).as(User)
  lazy val oauth2Session = OAuth2Session.find("id = {id}").on("id" -> userId).as(OAuth2Session)

  import controllers.UsingJson._

  // OAuth2 draft 10による認証成功
  it should "authorize a user with OAuth2" in {
    deleteDatabaseAndLoadFixtures
    val params = Map(
      "grant_type" -> "password",
      "username" -> user.email,
      "password" -> user.password
    )
    val response = POST("/token", params, emptyFiles)

    Logger.info("Token response was %s", getContent(response))

    val tokenResponse = read[TokenResponse](getContent(response))

    val numberOfSessionsForUser = OAuth2Session.count("userId = {userId}")
      .on("userId" -> user.id())
      .single()
    numberOfSessionsForUser should be (1)

    play.Logger.info("TokenResponse: %s", tokenResponse)
    val session = OAuth2Session.find("accessToken = {accessToken}")
      .on("accessToken" -> tokenResponse.accessToken.get)
      .as(OAuth2Session ?)
    session should not be (None)
  }

  // OAuth2認証付きPOSTリクエストの成功
  it should "add an achievement" in {
    deleteDatabaseAndLoadFixtures
    val response = POST(
      requestWithAuthorization(oauth2Header),
      userAchievementsUrl,
      Map("achievementId" -> achievementId),
      emptyFiles
    )
    response shouldBeOk()
  }

  // OAuth2認証付きGETリクエストの成功
  it should "retrieve user's achievement" in {
    deleteDatabaseAndLoadFixtures
    val response = GET(
      requestWithAuthorization(oauth2Header),
      userAchievementsUrl
    )
    Logger.info("Response for %s was %s", userAchievementsUrl, getContent(response))
    val userAchievementsResponse = read[UserAchievementsResponse](getContent(response))
    val achievements = userAchievementsResponse.achievements
    achievements.size should be (1)
    val a = achievements(0)
    a.title should be ("_titleOfAchievement1_")
  }

  // OAuth2認証に必要なヘッダが足りてないので400になるケース.
  // このケースはOAuth2の仕様で定められている
  it should "not add an achievement" in {
    deleteDatabaseAndLoadFixtures
    val response = POST(
      userAchievementsUrl,
      Map("achievementId" -> achievementId),
      emptyFiles
    )
    response.status should be (400)
  }

  // OAuth2認証に必要なヘッダはあるが、その中で指定されているアクセストークンが有効でないケース
  // このケースはOAuth2の仕様で定められている
  it should "not add an achievement with invalid token" in {
    deleteDatabaseAndLoadFixtures
    val response = POST(
      requestWithAuthorization(invalidOauth2Header),
      userAchievementsUrl,
      Map("achievementId" -> achievementId),
      emptyFiles
    )
    response.status should be (400)
  }
}
