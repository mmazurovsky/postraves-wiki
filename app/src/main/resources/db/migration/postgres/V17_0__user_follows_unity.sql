CREATE TABLE user_follows_unity
(
    user_profile_uid              VARCHAR(28)              REFERENCES user_profile(auth_uid) ON UPDATE CASCADE ON DELETE CASCADE,
    unity_id                      BIGINT                   REFERENCES unity(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (user_profile_uid, unity_id)
);