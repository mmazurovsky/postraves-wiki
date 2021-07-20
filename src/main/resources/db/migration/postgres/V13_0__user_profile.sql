CREATE TABLE user_profile
(
    auth_uid                 VARCHAR(28)              UNIQUE NOT NULL,
    created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    name                     VARCHAR(40)              UNIQUE NOT NULL,
    image_link               TEXT,
    about                    TEXT,
    base_rating              INTEGER,
    overall_followers_count  INTEGER                  NOT NULL,
    city_name                VARCHAR(40)              REFERENCES city(name),
    instagram_link           TEXT,
    telegram_link            TEXT,
    PRIMARY KEY (auth_uid)
);