package models

import org.scalatest.matchers.ShouldMatchers
import play.test._
import test.FixturesFromDataYml

class UserTest extends UnitFlatSpec with ShouldMatchers with FixturesFromDataYml {
  val accessToken = "accessToken1"

  it should "find user by access token." in {
    deleteDatabaseAndLoadFixtures()
    val user = User.findByAccessToken(accessToken)

    user should not be (None)
  }

}