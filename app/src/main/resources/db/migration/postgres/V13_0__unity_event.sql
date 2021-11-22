CREATE TABLE unity_event
(
    unity_event_unity_id                     BIGINT                      REFERENCES unity(unity_id) ON UPDATE CASCADE ON DELETE CASCADE,
    unity_event_event_id                     BIGINT                      REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (unity_event_unity_id, unity_event_event_id)
);