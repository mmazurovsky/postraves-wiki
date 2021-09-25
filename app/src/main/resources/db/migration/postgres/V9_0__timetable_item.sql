CREATE TABLE timetable_item
(
    timetable_item_id                          BIGSERIAL                UNIQUE NOT NULL,
    timetable_item_event_id                    BIGINT                   REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE,
    timetable_item_scene_id                    BIGINT                   REFERENCES scene(scene_id) ON UPDATE CASCADE ON DELETE CASCADE,
    timetable_item_created_date_time           TIMESTAMP                WITH TIME ZONE NOT NULL,
    timetable_item_starting_date_time          TIMESTAMP                WITH TIME ZONE,
    timetable_item_ending_date_time            TIMESTAMP                WITH TIME ZONE,
    timetable_item_type_of_performance         VARCHAR(60),
    PRIMARY KEY (timetable_item_id)
);