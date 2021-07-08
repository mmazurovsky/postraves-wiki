CREATE TABLE user_profile
(
    id                       BIGINT                   UNIQUE NOT NULL,
    auth_uid                 VARCHAR(28)              UNIQUE,
    created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    name                     VARCHAR(40)              UNIQUE NOT NULL,
    image_link               TEXT,
    about                    TEXT,
    base_rating              INTEGER,
    overall_followers_count  INTEGER                  NOT NULL,
    city_name                VARCHAR(40)              REFERENCES city(name),
    instagram_link           TEXT,
    telegram_link            TEXT,
    PRIMARY KEY (id)
);