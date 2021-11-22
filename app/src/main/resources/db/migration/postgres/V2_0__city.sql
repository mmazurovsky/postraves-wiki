CREATE TABLE city
(
    city_name              VARCHAR(40) UNIQUE       NOT NULL,
    city_name_ru           VARCHAR(40)              NOT NULL,
    city_name_en           VARCHAR(40)              NOT NULL,
    city_name_de           VARCHAR(40)              NOT NULL,
    city_name_fr           VARCHAR(40)              NOT NULL,
    city_created_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    city_time_offset       INT                      NOT NULL,
    city_country_name      VARCHAR(3) REFERENCES country (country_name) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (city_name)
);