# --- !Ups

CREATE TABLE UserAchievement (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  userId bigint(20) NOT NULL,
  achievementId bigint(20) NOT NULL,
  PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE UserAchievement;
