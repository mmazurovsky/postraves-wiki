CREATE TABLE user_follows_artist
(
    user_profile_uid              VARCHAR(28)              REFERENCES user_profile(auth_uid) ON UPDATE CASCADE ON DELETE CASCADE,
    artist_id                     BIGINT                   REFERENCES artist(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (user_profile_uid, artist_id)
);