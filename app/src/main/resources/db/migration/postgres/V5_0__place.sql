CREATE TABLE place
(
    id                       BIGSERIAL                UNIQUE NOT NULL,
    created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    name                     VARCHAR(60)              UNIQUE NOT NULL,
    image_link               TEXT,
    about                    TEXT,
    city_name                VARCHAR(40)              REFERENCES city(name) ON UPDATE CASCADE ON DELETE CASCADE,
    street_address           TEXT                     NOT NULL,
    latitude                 double precision         NOT NULL,
    longitude                double precision         NOT NULL,
    instagram_link           TEXT,
    soundcloud_link          TEXT,
    PRIMARY KEY (id)
);