CREATE TABLE event
(
    event_id                       BIGSERIAL                UNIQUE NOT NULL,
    event_created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    event_updated_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    event_name                     VARCHAR(80)              UNIQUE NOT NULL,
    event_image_link               TEXT,
    event_about                    TEXT,
    event_is_cancelled             BOOLEAN                  NOT NULL,
    event_start_date_time          TIMESTAMP                WITH TIME ZONE NOT NULL,
    event_end_date_time            TIMESTAMP                WITH TIME ZONE NOT NULL,
    event_tickets_link             TEXT,
    event_place_id                 BIGINT                   REFERENCES place(place_id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (event_id)
);