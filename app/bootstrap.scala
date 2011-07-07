import play.jobs._
import play.libs.Crypto
import play.Logger
import util.matching.Regex.Match

@OnApplicationStart
class bootstrap extends Job {
  override def doJob {
    import models._
    import play.test._

    val examplePassword = "1234"
    Logger.info("Password hash for %s is %s", examplePassword, Crypto.passwordHash(examplePassword))

    val exampleEmail = "mumoshu@example.com"
    Logger.info("AES encryption for %s is %s", exampleEmail, Crypto.encryptAES(exampleEmail))

    if (Avatar.count().single() == 0 && User.count().single() == 0) {
      Yaml[List[Any]]("initial-data.yml").foreach {
        case user:User => User.create(user)
        case avatar:Avatar => Avatar.create(avatar)
        case lobby:Lobby => Lobby.create(lobby)
        case _ => ()
      }
    }
  }
}