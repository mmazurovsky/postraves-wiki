CREATE TABLE user_follows_unity
(
    user_follows_unity_user_profile_id               BIGINT                   REFERENCES user_profile(user_profile_id) ON UPDATE CASCADE ON DELETE CASCADE,
    user_follows_unity_unity_id                      BIGINT                   REFERENCES unity(unity_id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (user_follows_unity_user_profile_id, user_follows_unity_unity_id)
);