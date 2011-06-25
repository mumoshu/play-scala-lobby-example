# --- !Ups

ALTER TABLE Achievement ADD COLUMN score int(4) NOT NULL;

# --- !Downs

ALTER TABLE Achievement DROP COLUMN score;
