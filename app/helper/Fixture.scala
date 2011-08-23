package helper

import play.test._
import models._

/**
 * @author KUOKA Yusuke
 */

object Fixture {
  def deleteDatabaseAndLoadFixtures {
    Fixtures.deleteDatabase()
    Yaml[List[Any]]("data.yml").foreach {
      _ match {
        case u: User => User.create(u)
        case a: Achievement => Achievement.create(a)
        case ua: UserAchievement => UserAchievement.create(ua)
        case os: OAuth2Session => OAuth2Session.create(os)
        case avatar: Avatar => Avatar.create(avatar)
        case game: Game => Game.create(game)
        case lobby: Lobby => Lobby.create(lobby)
        case _ => ()
      }
    }
  }
}