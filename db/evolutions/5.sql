# --- !Ups

CREATE TABLE WebSocketAuthorization (
  userId bigint(20) NOT NULL AUTO_INCREMENT,
  sessionId varchar(20) NOT NULL,
  PRIMARY KEY (userId)
)

# --- !Downs

DROP TABLE WebSocketAuthorization;
