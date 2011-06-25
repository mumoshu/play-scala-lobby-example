import java.io.File
import org.scalatest.matchers.ShouldMatchers
import play.test.{FunctionalFlatSpec, FunctionalTest}
import collection.JavaConversions._

class LobbyTest extends FunctionalFlatSpec with ShouldMatchers {
  it should "show the user registration page" in {
    val response = GET("/users/join")
    response shouldBeOk()
    getContent(response) should not include ("Error")
  }

  it should "create a new user" in {
    val parameters = Map(
      "name" -> "_name_",
      "password" -> "_password_",
      "email" -> "_email_"
    )
    val response = POST("/users/create", parameters, Map[String, File]())
    response shouldBeOk()
    // How do you test redirections from POST requets?
//    response headerShouldBe("Location", "/users/created")
  }
}