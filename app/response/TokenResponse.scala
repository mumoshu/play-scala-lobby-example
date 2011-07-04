package response

/**
 * Access Token Response defined in the OAuth2 spec. draft 10.
 * See http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.2
 */
case class TokenResponse(
  val error: String,
  val errorDescription: String,
  val accessToken: String
)
