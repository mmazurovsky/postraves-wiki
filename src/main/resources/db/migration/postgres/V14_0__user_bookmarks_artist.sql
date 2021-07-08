CREATE TABLE user_bookmarks_artist
(
    user_profile_id               BIGINT                   REFERENCES user_profile(id) ON UPDATE CASCADE ON DELETE CASCADE,
    artist_id                     BIGINT                   REFERENCES artist(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (user_profile_id, artist_id)
);