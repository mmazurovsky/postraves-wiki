CREATE TABLE event
(
    id                       BIGINT                   UNIQUE NOT NULL,
    created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    name                     VARCHAR(80)              UNIQUE NOT NULL,
    image_link               TEXT,
    about                    TEXT,
    base_rating              INTEGER,
    overall_followers_count  INTEGER                  NOT NULL,
    status                   TEXT                     NOT NULL,
    start_date_time          TIMESTAMP                WITH TIME ZONE NOT NULL,
    end_date_time            TIMESTAMP                WITH TIME ZONE NOT NULL,
    tickets_link             TEXT,
    place_id                 BIGINT                   REFERENCES place(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (id)
);