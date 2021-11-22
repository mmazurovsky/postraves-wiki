CREATE TABLE user_follows_artist
(
    user_follows_artist_user_profile_id               BIGINT              REFERENCES user_profile(user_profile_id) ON UPDATE CASCADE ON DELETE CASCADE,
    user_follows_artist_artist_id                     BIGINT              REFERENCES artist(artist_id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (user_follows_artist_user_profile_id, user_follows_artist_artist_id)
);