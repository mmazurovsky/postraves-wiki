CREATE TABLE user_follows_unity
(
    user_follows_unity_user_profile_uid              VARCHAR(28)              REFERENCES user_profile(user_profile_auth_uid) ON UPDATE CASCADE ON DELETE CASCADE,
    user_follows_unity_unity_id                      BIGINT                   REFERENCES unity(unity_id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (user_follows_unity_user_profile_uid, user_follows_unity_unity_id)
);