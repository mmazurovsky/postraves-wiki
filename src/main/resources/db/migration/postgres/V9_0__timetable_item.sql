CREATE TABLE timetable_item
(
    id                          BIGINT                   UNIQUE NOT NULL,
    event_id                    BIGINT                   REFERENCES event(id) ON UPDATE CASCADE ON DELETE CASCADE,
    scene_id                    BIGINT                   REFERENCES scene(id) ON UPDATE CASCADE ON DELETE CASCADE,
    created_date_time           TIMESTAMP                WITH TIME ZONE NOT NULL,
    starting_date_time          TIMESTAMP                WITH TIME ZONE,
    ending_date_time            TIMESTAMP                WITH TIME ZONE,
    type_of_performance         VARCHAR(60),
    PRIMARY KEY (id)
);