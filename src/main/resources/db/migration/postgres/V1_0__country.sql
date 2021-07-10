-- CREATE SEQUENCE id_sequence
--     INCREMENT 1
--     MINVALUE 1
--     MAXVALUE 9223372036854775807
--     START 1
--     CACHE 10;

CREATE TABLE country
(
    name                     VARCHAR(3)               UNIQUE NOT NULL,
    created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    phone_code               VARCHAR(15)              UNIQUE NOT NULL,
    emoji_code               VARCHAR(20)              UNIQUE NOT NULL,
    PRIMARY KEY (name)
);