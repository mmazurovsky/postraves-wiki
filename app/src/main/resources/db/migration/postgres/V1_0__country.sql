CREATE TABLE country
(
    country_name                     VARCHAR(3)               UNIQUE NOT NULL,
    country_name_ru                  VARCHAR(40)              NOT NULL,
    country_name_en                  VARCHAR(40)              NOT NULL,
    country_name_de                  VARCHAR(40)              NOT NULL,
    country_name_fr                  VARCHAR(40)              NOT NULL,
    country_created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    country_phone_code               VARCHAR(15)              UNIQUE NOT NULL,
    country_emoji_code               VARCHAR(20)              UNIQUE NOT NULL,
    PRIMARY KEY (country_name)
);