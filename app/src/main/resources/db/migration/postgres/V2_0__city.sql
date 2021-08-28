CREATE TABLE city
(
    name                     VARCHAR(40)              UNIQUE NOT NULL,
    created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    time_offset              INT                      NOT NULL,
    country_name             VARCHAR(3)               REFERENCES country(name) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (name)
);