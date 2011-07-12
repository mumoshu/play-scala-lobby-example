package utils

import play.mvc.Router
import play.templates.GroovyTemplate
import play.templates.GroovyTemplate.ExecutableTemplate
import play.{Logger, Play}
import java.util.HashMap

object RouteUtil {
  import collection.JavaConversions._

  def webSocketChatRoom(title: String, username: String) = {
    val args = new HashMap[String, AnyRef]()
    args.put("title", title)
    args.put("username", username)

    val actionDefinition = Router.reverse("RoomsSocket.join", args)
    actionDefinition.absolute()
    actionDefinition.url
  }
}