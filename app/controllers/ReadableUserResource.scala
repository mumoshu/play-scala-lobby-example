package controllers

import models.User
import net.liftweb.json.Serialization.{read, write}
import net.liftweb.json._
import play.db.anorm.{Id, NotAssigned, Pk, SQL}
import play.db.anorm._
import play.db.anorm.defaults._
import play.mvc.Controller
import scala.Some
import xsbti.api.Protected

class Result(val status: Int)
case class Query(id: Long)
case class Found(user: User) extends Result(200)
case class NotFoundError(error: String = "resource_not_found") extends Result(404)
case class GetResourcesResponse(users: List[User]) extends Result(200)

trait ReadableResource[K, T] {
  self: Controller =>

  // Override the below at least.
  protected def parser(): Magic[T]
  protected implicit val manifestForResourceClass: Manifest[T]

  // You can provide an another serializer overriding val serializer.
  protected val serializer = FieldSerializer[T]()

  // You can provide an another plural for the resource name,
  // or singular + 's' is used by default.
  protected val plural: Option[String] = None

  protected implicit val jsonFormats = DefaultFormats + serializer + new PkSerializer

  protected def toLowerSnakeCase(s: String) = {
    s.replaceAll(
      String.format("%s|%s|%s",
        "(?<=[A-Z])(?=[A-Z][a-z])",
        "(?<=[^A-Z])(?=[A-Z])",
        "(?<=[A-Za-z])(?=[^A-Za-z])"
      ),
      "_"
    ).toLowerCase
  }

  protected def pluralize(singular: String) = {
    plural match {
      case Some(p) => p
      case None => singular + "s"
    }
  }

  def getResource(id: Long) = {
    val option = parser.find("id={id}").on("id" -> id).as(parser ?)
    val text = option match {
      case Some(resource) => write(Map("status" -> 200, toLowerSnakeCase(parser.analyser.name) -> resource))
      case None => write(Map("status" -> 404, "error" -> "resource_not_found"))
    }
    Json(text)
  }

  def getResources() = {
    val resources = SQL("select * from " + parser.analyser.name).as(parser *)
    val text = write(Map("status" -> 200, pluralize(parser.analyser.name) -> resources))
    Json(text)
  }
}

trait ReadableUserResource {
  self: Controller =>

//  implicit val formats = Serialization.formats(ShortTypeHints(List(
//    classOf[User], classOf[Found], classOf[NotFoundError]
//  )))
  implicit val formats = DefaultFormats + FieldSerializer[User]() + FieldSerializer[Result]() + new PkSerializer

  def getResource(id: Long) = {
    val userOption = User.find("id={id}").on("id" -> id).as(User ?)
    val text = userOption match {
      case Some(user) => write(Found(user))
      case _ => write(NotFoundError())
    }
    Json(text)
  }

  def getResources() = {
    val users = SQL("select * from " + User.analyser.name).as(User *)
    Json(write(GetResourcesResponse(users)))
  }
}

/**
 * Serializes Pk to {@link JInt} or {@link JString}.
 */
class PkSerializer extends Serializer[Pk[_]] {
  private val PkClass = classOf[Pk[_]]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Pk[_]] = {
    case (TypeInfo(PkClass, _), json) => json match {
      case JInt(int) => Id(int)
      case JString(str) => Id(str)
      case x => throw new MappingException("Can't convert " + x + " to Pk")
    }
  }
  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: Pk[_] => JInt(x.toString.toInt)
  }

}