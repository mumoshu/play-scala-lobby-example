package response

import models.Achievement

/**
 * ユーザの実績リストのレスポンス
 */
case class UserAchievements(val achievements: List[Achievement])
