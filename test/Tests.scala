import models.Achievement
import play._
import play.test._

import org.scalatest._
import org.scalatest.junit._
import org.scalatest.matchers._

import play.db.anorm._

class BasicTests extends UnitFlatSpec with ShouldMatchers {

    it should "create and retrieve an Achievement" in {
      val created = Achievement.create(Achievement(NotAssigned, "title", "descr", "imageUrl"))
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
    
    it should "run this dumb test" in {
        (1 + 1) should be (2)
    }

}