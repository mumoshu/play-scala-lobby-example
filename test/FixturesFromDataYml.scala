package test

import play.test._
import models._

trait FixturesFromDataYml {
  def deleteDatabaseAndLoadFixtures() = {
    Fixtures.deleteDatabase()
    Yaml[List[Any]]("data.yml").foreach {
      case u: User => User.create(u)
      case a: Achievement => Achievement.create(a)
      case s: OAuth2Session => OAuth2Session.create(s)
      case ua: UserAchievement => UserAchievement.create(ua)
    }
  }
}
