CREATE TABLE user_follows_event
(
    user_follows_event_user_profile_id               BIGINT                   REFERENCES user_profile(user_profile_id) ON UPDATE CASCADE ON DELETE CASCADE,
    user_follows_event_event_id                      BIGINT                   REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (user_follows_event_user_profile_id, user_follows_event_event_id)
);