ALTER TABLE artist DROP CONSTRAINT artist_artist_name_key;
ALTER TABLE event DROP CONSTRAINT event_event_name_key;
ALTER TABLE place DROP CONSTRAINT place_place_name_key;
ALTER TABLE unity DROP CONSTRAINT unity_unity_name_key;
CREATE INDEX artist_artist_name_index ON artist(artist_name);
CREATE INDEX event_event_name_index ON event(event_name);
CREATE INDEX place_place_name_index ON place(place_name);
CREATE INDEX unity_unity_name_index ON unity(unity_name);
