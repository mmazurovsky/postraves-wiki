CREATE TABLE user_follows_event
(
    user_follows_event_user_profile_uid              VARCHAR(28)              REFERENCES user_profile(user_profile_auth_uid) ON UPDATE CASCADE ON DELETE CASCADE,
    user_follows_event_event_id                      BIGINT                   REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (user_follows_event_user_profile_uid, user_follows_event_event_id)
);