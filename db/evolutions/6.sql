# --- !Ups

CREATE TABLE Avatar (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  iconPath varchar(255),
  PRIMARY KEY (id)
)

# === !Downs

DROP TABLE Avatar;
