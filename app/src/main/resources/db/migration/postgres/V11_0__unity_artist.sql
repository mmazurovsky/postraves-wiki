CREATE TABLE unity_artist
(
    unity_id                      BIGINT                REFERENCES unity(id) ON UPDATE CASCADE ON DELETE CASCADE,
    artist_id                     BIGINT                REFERENCES artist(id) ON UPDATE CASCADE ON DELETE CASCADE,
    is_founder                    boolean                  NOT NULL DEFAULT FALSE,
    PRIMARY KEY (unity_id, artist_id)
);