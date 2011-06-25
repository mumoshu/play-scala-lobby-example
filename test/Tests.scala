import models.{UserAchievement, User, Achievement}
import play._
import libs.Crypto
import play.test._

import org.scalatest._
import matchers._
import org.scalatest.junit._
import play.db.anorm._
import play.db.anorm.defaults._
import play.db.anorm.SqlParser._

class BasicTests extends UnitFlatSpec with ShouldMatchers {

    it should "create and retrieve an Achievement" in {
      val created = Achievement.create(Achievement(NotAssigned, "title", "descr", 100, "imageUrl"))
      val a = Achievement.find("title={title}")
        .on("title" -> "title")
        .first()
      a should not be (None)
      a.get.title should be ("title")
      a.get.description should be ("descr")
      a.get.imageUrl should be("imageUrl")
    }

  it should "load fixtures and retrieve an Achievement" in {
    Fixtures.deleteDatabase()
    Yaml[List[Any]]("data.yml").foreach {
      _ match {
        case a: Achievement => Achievement.create(a)
        case _ => ()
      }
    }
    val aOption = Achievement.find("title={title}")
      .on("title" -> "_titleOfAchievement1_")
      .first()
    aOption should not be (None)
    val a = aOption.get
    a.title should be ("_titleOfAchievement1_")
    a.description should be ("_descriptionOfAchievement1_")
  }

  it should "create and save a User" in {
    val email: String = "email"
    val password: String = "password"
    val name: String = "name"
    val user = User.join(name, password, email)
    user.name should be (name)
    user.password should be (Crypto.passwordHash(password))
    user.email should be (Crypto.encryptAES(email))
  }

  it should "retrieve a User with findByEmailAndPassword" in {
    Fixtures.deleteDatabase()
    Yaml[List[Any]]("data.yml").foreach {
      _ match {
        case u: User => User.create(u)
        case _ => ()
      }
    }
    val email = "_emailOfUser1_"
    val password = "_passwordOfUser1_"
    val encryptedEmail = Crypto.encryptAES(email)
    val passwordHash = Crypto.passwordHash(password)

    Logger.info("email should be %s", encryptedEmail)
    Logger.info("password should be %s", passwordHash)

    val userOption = User.findByEmailAndPassword(email, password)
    userOption should not be (None)
    val Some(user) = userOption
    user.name should be ("_nameOfUser1_")
    user.password should be (passwordHash)
    user.email should be (encryptedEmail)
  }

  it should "retrieve a User with an Achievement" in {
    Fixtures.deleteDatabase()
    Yaml[List[Any]]("data.yml").foreach {
      _ match {
        case u: User => User.create(u)
        case a: Achievement => Achievement.create(a)
        case ua: UserAchievement => UserAchievement.create(ua)
      }
    }

    val Some(user~achievements) = User.findByIdWithAchievements(1)
    user.name should be ("_nameOfUser1_")
    user.password should be (Crypto.passwordHash("_passwordOfUser1_"))
    user.email should be (Crypto.encryptAES("_emailOfUser1_"))

    achievements.size should be (1)

    val firstAchievement = achievements(0)
    firstAchievement.title should be ("_titleOfAchievement1_")
    firstAchievement.description should be ("_descriptionOfAchievement1_")
  }
    
    it should "run this dumb test" in {
        (1 + 1) should be (2)
    }

}