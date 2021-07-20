CREATE TABLE user_follows_event
(
    user_profile_uid              VARCHAR(28)              REFERENCES user_profile(auth_uid) ON UPDATE CASCADE ON DELETE CASCADE,
    event_id                      BIGINT                   REFERENCES event(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (user_profile_uid, event_id)
);