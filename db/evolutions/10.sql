# --- !Ups

ALTER TABLE LOBBY ADD COLUMN gameId bigint(20) NOT NULL

# --- !Downs

ALTER TABLE LOBBY DROP COLUMN gameId
