CREATE TABLE user_profile
(
    user_profile_auth_uid                 VARCHAR(28)              UNIQUE NOT NULL,
    user_profile_created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    user_profile_name                     VARCHAR(40)              UNIQUE NOT NULL,
    user_profile_image_link               TEXT,
    user_profile_about                    TEXT,
    user_profile_city_name                VARCHAR(40)              REFERENCES city(city_name) ON UPDATE CASCADE ON DELETE SET NULL,
    user_profile_instagram_link           TEXT,
    user_profile_telegram_link            TEXT,
    PRIMARY KEY (user_profile_auth_uid)
);