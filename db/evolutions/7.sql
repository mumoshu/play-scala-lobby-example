# --- !Ups

CREATE TABLE OAUTH2SESSION (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  userId bigint(20) NOT NULL,
  accessToken varchar(255) NOT NULL,
  PRIMARY KEY (id)
)

# --- !Downs

DROP TABLE OAUTH2SESSION
