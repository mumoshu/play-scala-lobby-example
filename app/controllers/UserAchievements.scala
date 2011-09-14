package controllers

import play.data.validation.Validation
import play.Logger
import response.UserAchievementsResponse
import play.mvc._
import net.liftweb.json.Serialization._
import models.{User, Achievement, UserAchievement}
import play.db.anorm._
import play.db.anorm.defaults._

object UserAchievements extends Controller with OAuth with Secure {
  import controllers.UsingJson._
  import views.UserAchievements._

  override val needsOauthOnlyFor = Set("find", "create")
  override val needsSessionOnlyFor = Set("index")

  @Before
  def fixRequestInJson = {
    // A tiny hack to send the response in JSON.
    request.format = "json"
    Continue
  }

  /**
   * Finds all achievements for the user.
   */
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

  /**
   * Creates an achievement for the user.
   */
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

  /**
   * List user's all achievements in a Web page.
   */
  def index() = html.index(
    user,
    SQL("""
      select * from UserAchievement u left join Achievement a
      where u.achievementId = a.id and u.userId = {userId}
    """).on("userId" -> user)
      .as(Achievement *)
  )

}