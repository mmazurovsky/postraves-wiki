CREATE TABLE country
(
    name                     VARCHAR(3)               UNIQUE NOT NULL,
    name_ru                  VARCHAR(40)              NOT NULL,
    name_uk                  VARCHAR(40)              NOT NULL,
    name_de                  VARCHAR(40)              NOT NULL,
    name_fr                  VARCHAR(40)              NOT NULL,
    created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    phone_code               VARCHAR(15)              UNIQUE NOT NULL,
    emoji_code               VARCHAR(20)              UNIQUE NOT NULL,
    PRIMARY KEY (name)
);