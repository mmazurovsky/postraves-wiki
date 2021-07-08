CREATE TABLE user_bookmarks_event
(
    user_profile_id               BIGINT                  REFERENCES user_profile(id) ON UPDATE CASCADE ON DELETE CASCADE,
    event_id                      BIGINT                   REFERENCES event(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (user_profile_id, event_id)
);