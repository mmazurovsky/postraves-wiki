CREATE TABLE user_follows_place
(
    user_profile_uid              VARCHAR(28)              REFERENCES user_profile(auth_uid) ON UPDATE CASCADE ON DELETE CASCADE,
    place_id                      BIGINT                   REFERENCES place(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (user_profile_uid, place_id)
);