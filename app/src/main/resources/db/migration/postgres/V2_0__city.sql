CREATE TABLE city
(
    name                     VARCHAR(40)              UNIQUE NOT NULL,
    name_ru                  VARCHAR(40)              NOT NULL,
    name_uk                  VARCHAR(40)              NOT NULL,
    name_de                  VARCHAR(40)              NOT NULL,
    name_fr                  VARCHAR(40)              NOT NULL,
    created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    time_offset              INT                      NOT NULL,
    country_name             VARCHAR(3)               REFERENCES country(name) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (name)
);