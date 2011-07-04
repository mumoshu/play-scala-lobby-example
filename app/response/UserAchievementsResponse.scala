package response

import models.Achievement

/**
 * ユーザの実績リストのレスポンス
 */
case class UserAchievementsResponse(
  val error: Option[String],
  val errorDescription: Option[String],
  val achievements: List[Achievement]
)
