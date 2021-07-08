CREATE TABLE unity_event
(
    unity_id                     BIGINT                      REFERENCES unity(id) ON UPDATE CASCADE ON DELETE CASCADE,
    event_id                     BIGINT                      REFERENCES event(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (unity_id, event_id)
);