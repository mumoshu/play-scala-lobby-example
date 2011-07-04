package response

/**
 * Access Token Response defined in the OAuth2 spec. draft 10.
 * See http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.2
 */
case class TokenResponse(
  val error: Option[String],
  val errorDescription: Option[String],
  val accessToken: Option[String]
) {
  // A workaround for lift-json.
  // The constructor with all optional params seems to not used by lift-json.
  // I had to give the below auxiary constructor to parse a json serialized from
  // <code>TokenResponse(None, None, Option(accessToken))</code>
  def this(accessToken: String) = this(None, None, Option(accessToken))
}
