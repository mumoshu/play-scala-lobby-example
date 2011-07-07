package oauth

case class InvalidRequest(val error: String = "invalid_request")
