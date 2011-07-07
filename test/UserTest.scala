package models

import org.scalatest.matchers.ShouldMatchers
import play.test._
import test.FixturesFromDataYml

class UserTest extends UnitFlatSpec with ShouldMatchers with FixturesFromDataYml {
  val accessToken = "_accessToken_"

  it should "find user by access token." in {
    val user = User.findByAccessToken(accessToken)

    user should not be (None)
  }

}