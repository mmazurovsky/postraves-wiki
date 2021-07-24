CREATE TABLE artist
(
    id                       BIGSERIAL                UNIQUE NOT NULL,
    created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    name                     VARCHAR(60)              UNIQUE NOT NULL,
    image_link               TEXT,
    about                    TEXT,
    country_name             VARCHAR(3)               REFERENCES country(name) ON UPDATE CASCADE ON DELETE CASCADE,
    instagram_link           TEXT,
    soundcloud_link          TEXT,
    PRIMARY KEY (id)
);