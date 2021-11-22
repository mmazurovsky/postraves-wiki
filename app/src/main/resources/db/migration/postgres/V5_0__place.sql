CREATE TABLE place
(
    place_id                  BIGSERIAL UNIQUE         NOT NULL,
    place_created_date_time   TIMESTAMP WITH TIME ZONE NOT NULL,
    place_name                VARCHAR(60) UNIQUE       NOT NULL,
    place_image_link          TEXT,
    place_about               TEXT,
    place_city_name           VARCHAR(40) REFERENCES city (city_name) ON UPDATE CASCADE ON DELETE CASCADE,
    place_street_address      TEXT                     NOT NULL,
    place_latitude            double precision         NOT NULL,
    place_longitude           double precision         NOT NULL,
    place_instagram_username  TEXT,
    place_soundcloud_username TEXT,
    place_is_just_city        BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (place_id)
);