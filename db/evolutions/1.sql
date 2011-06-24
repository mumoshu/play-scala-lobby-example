# --- !Ups

CREATE TABLE Achievement (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  title varchar(255) NOT NULL,
  description varchar(255) NOT NULL,
  imageUrl varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE Achievement;
