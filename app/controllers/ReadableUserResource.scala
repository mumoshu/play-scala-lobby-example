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
import play.mvc.results.RenderJson
import play.Logger

class Result(val status: Int)
case class Query(id: Long)
case class Found(user: User) extends Result(200)
case class NotFoundError(error: String = "resource_not_found") extends Result(404)
case class GetResourcesResponse(users: List[User]) extends Result(200)

trait ReadableResource[T <: ScalaObject] {
  self: Controller =>
  // Override the below at least.
  implicit val resourceManifest: Manifest[T]

//  val resourceParser: Magic[T]
  object resourceParser extends Magic[T]

  // You can provide an another serializer overriding val serializer.
  // "FieldSerializer[T]()" this causes an ExceptionInitializerError.
  // You need to write as "FieldSerializer[Manifest[T]]()"
  //
  // In lift-json examples, you write like this.
  // <code>
  // case class WildDog()
  // ...
  // FieldSerializer[WildDog]()
  // </code>
  // Maybe it works because Scala itself implicitly converts WildDog to Manifest[WildDog]
  // for FieldSerializer's type parameter.
  // But it's not the case while generic programming.
//  protected val serializer = FieldSerializer[Manifest[T]]()

  // You can provide an another plural for the resource name,
  // or singular + 's' is used by default.
  protected val plural: Option[String] = None

  protected implicit val jsonFormats = DefaultFormats + FieldSerializer[AnyRef]() + new PkSerializer

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

  def getMap = {
    new RenderJson(write(Map("key" -> "val")))
  }

  def getResource(id: String) = {
    val option = resourceParser.find("id={id}").on("id" -> id).as(resourceParser ?)
    val text = option match {
      case Some(resource) => {
        val t = write(Map("status" -> 200, toLowerSnakeCase(resourceParser.analyser.name) -> resource))
        Logger.info(resource.toString)
        t
      }
      case None => write(Map("status" -> 404, "error" -> "resource_not_found"))
    }
    Json(text)
  }

  def getResources() = {
    val resources = SQL("select * from " + resourceParser.analyser.name).as(resourceParser *)
    val text = write(Map("status" -> 200, pluralize(resourceParser.analyser.name) -> resources))
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