CREATE TABLE unity
(
    unity_id                       BIGSERIAL                UNIQUE NOT NULL,
    unity_created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    unity_name                     VARCHAR(60)              UNIQUE NOT NULL,
    unity_image_link               TEXT,
    unity_about                    TEXT,
    unity_country_name             VARCHAR(3)               REFERENCES country(country_name) ON UPDATE CASCADE ON DELETE CASCADE,
    unity_instagram_username           TEXT,
    unity_soundcloud_username          TEXT,
    unity_bandcamp_username            TEXT,
    PRIMARY KEY (unity_id)
);