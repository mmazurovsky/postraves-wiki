CREATE TABLE unity_artist
(
    unity_artist_unity_id                      BIGINT                REFERENCES unity(unity_id) ON UPDATE CASCADE ON DELETE CASCADE,
    unity_artist_artist_id                     BIGINT                REFERENCES artist(artist_id) ON UPDATE CASCADE ON DELETE CASCADE,
    unity_artist_is_founder                    boolean               NOT NULL DEFAULT FALSE,
    PRIMARY KEY (unity_artist_unity_id, unity_artist_artist_id)
);