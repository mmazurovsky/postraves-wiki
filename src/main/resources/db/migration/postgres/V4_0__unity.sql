CREATE TABLE unity
(
    id                       BIGINT                   UNIQUE NOT NULL,
    auth_uid                 VARCHAR(28)              UNIQUE,
    created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    name                     VARCHAR(60)              UNIQUE NOT NULL,
    image_link               TEXT,
    about                    TEXT,
    base_rating              INTEGER,
    overall_followers_count  INTEGER                  NOT NULL,
    country_name             VARCHAR(3)               REFERENCES country(name) ON UPDATE CASCADE ON DELETE CASCADE,
    instagram_link           TEXT,
    soundcloud_link          TEXT,
    bandcamp_link            TEXT,
    PRIMARY KEY (id)
);