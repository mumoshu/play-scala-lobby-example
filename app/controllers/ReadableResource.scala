package controllers

import models.User
import net.liftweb.json.Serialization.{read, write}
import play.mvc.Controller
import net.liftweb.json._
import play.db.anorm.{Id, NotAssigned, Pk}

class Result(val status: Int)
case class Query(id: Long)
case class Found(user: User) extends Result(200)
case class NotFoundError(error: String = "resource_not_found") extends Result(404)

trait ReadableResource {
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