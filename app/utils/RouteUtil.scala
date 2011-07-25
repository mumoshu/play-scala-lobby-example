package utils

import play.mvc.Router
import play.templates.GroovyTemplate
import play.templates.GroovyTemplate.ExecutableTemplate
import play.{Logger, Play}
import java.util.HashMap

object RouteUtil {
  import collection.JavaConversions._

  def webSocketChatRoom(roomId: Long, userId: Long) = {
    val args = new HashMap[String, AnyRef]()
    args.put("roomId", roomId.asInstanceOf[AnyRef])
    args.put("userId", userId.asInstanceOf[AnyRef])

    val actionDefinition = Router.reverse("RoomsSocket.join", args)
    actionDefinition.absolute()
    actionDefinition.url
  }
}