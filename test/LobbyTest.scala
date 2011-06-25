import java.io.File
import models._
import org.scalatest.matchers.ShouldMatchers
import collection.JavaConversions._
import play.test.{Fixtures, FunctionalFlatSpec, FunctionalTest, Yaml}

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

  it should "retrieve a user in JSON" in {
    Fixtures.deleteDatabase()
    Yaml[List[Any]]("data.yml").foreach {
      _ match {
        case u: User => User.create(u)
        case _ => ()
      }
    }
    val response = GET("/api/users/1")
    response shouldBeOk()
    response contentShouldBe ("""{"user":{"id":1,"name":"_nameOfUser1_","password":"+h2/6IXSRxB8cxvP/yfikA==","email":"455f9d5d9a5883f8c4e34eb58d970745"},"status":200}""")
  }
}