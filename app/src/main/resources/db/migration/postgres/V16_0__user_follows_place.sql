CREATE TABLE user_follows_place
(
    user_follows_place_user_profile_id               BIGINT              REFERENCES user_profile(user_profile_id) ON UPDATE CASCADE ON DELETE CASCADE,
    user_follows_place_place_id                      BIGINT              REFERENCES place(place_id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (user_follows_place_user_profile_id, user_follows_place_place_id)
);