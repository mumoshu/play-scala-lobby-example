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
import response.UserAchievements

trait RestTestsHelpers {
  def requestWithAuthorization(header: Header) = {
    val request = new Request()
    request.headers.put("Authorization", header)
    request
  }
}

trait RestTestsConstants {
  val userId = 1L
  val accessToken = "_accessToken_"
  val oauth2Header = new Header(
    "Authorization",
    "OAuth2 " + accessToken
  )
  val invalidOauth2Header = new Header(
    "Authorization",
    "OAuth2 invalidAccessToken"
  )
  val emptyFiles = Map.empty[String, File]
  val achievementId = "1"
  val addUsersAchievementUrl = "/users/1/achievements"
}

class RestTests extends FunctionalFlatSpec with ShouldMatchers with RestTestsHelpers with RestTestsConstants {
  def loadFixture {
    Fixtures.deleteDatabase()
    Yaml[List[Any]]("data.yml").foreach {
      _ match {
        case u: User => User.create(u)
        case a: Achievement => Achievement.create(a)
        case _ => ()
      }
    }
  }

  lazy val user = User.find("id = {id}").on("id" -> userId).as(User)
  lazy val oauth2Session = OAuth2Session.find("id = {id}").on("id" -> userId).as(OAuth2Session)
  implicit val jsonFormats = DefaultFormats + FieldSerializer[AnyRef]()

  // OAuth2 draft 10による認証成功
  it should "authorize a user with OAuth2" in {
    val params = Map(
      "grant_type" -> "password",
      "username" -> user.email,
      "password" -> user.password
    )
    val response = POST("/token", params, emptyFiles)

    val tokenResponse = read[TokenResponse](getContent(response))
    tokenResponse.accessToken should be (accessToken)

    val oauth2Sessions = OAuth2Session.find("userId = {userId}")
      .on("userId" -> user.id)
      .as(User *)
    oauth2Sessions.size should be (1)
  }

  // OAuth2認証付きPOSTリクエストの成功
  it should "add an achievement" in {
    val response = POST(
      requestWithAuthorization(oauth2Header),
      addUsersAchievementUrl,
      Map("achievementId" -> achievementId),
      emptyFiles
    )
    response shouldBeOk()
  }

  // OAuth2認証付きGETリクエストの成功
  it should "retrieve user's achievement" in {
    val response = GET(
      requestWithAuthorization(oauth2Header),
      "/users/1/achievements"
    )
    val userAchievementsResponse = read[UserAchievements](getContent(response))
    val achievements = userAchievementsResponse.achievements
    achievements.size should be (1)
    val a = achievements(0)
    a.title should be ("_titleOfAchievement1_")
  }

  // OAuth2認証に必要なヘッダが足りてないので400になるケース.
  // このケースはOAuth2の仕様で定められている
  it should "not add an achievement" in {
    val response = POST(
      addUsersAchievementUrl,
      Map("achievementId" -> achievementId),
      emptyFiles
    )
    response.status should be (400)
  }

  // OAuth2認証に必要なヘッダはあるが、その中で指定されているアクセストークンが有効でないケース
  // このケースはOAuth2の仕様で定められている
  it should "not add an achievement with invalid token" in {
    val response = POST(
      requestWithAuthorization(invalidOauth2Header),
      addUsersAchievementUrl,
      Map("achievementId" -> achievementId),
      emptyFiles
    )
    response.status should be (401)
  }
}
