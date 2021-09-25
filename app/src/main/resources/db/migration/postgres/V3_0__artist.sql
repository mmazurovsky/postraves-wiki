CREATE TABLE artist
(
    artist_id                       BIGSERIAL                UNIQUE NOT NULL,
    artist_created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    artist_name                     VARCHAR(60)              UNIQUE NOT NULL,
    artist_image_link               TEXT,
    artist_about                    TEXT,
    artist_country_name             VARCHAR(3)               REFERENCES country(country_name) ON UPDATE CASCADE ON DELETE CASCADE,
    artist_instagram_link           TEXT,
    artist_soundcloud_link          TEXT,
    PRIMARY KEY (artist_id)
);