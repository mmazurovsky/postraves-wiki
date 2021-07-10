CREATE TABLE scene
(
    id                       BIGSERIAL                UNIQUE NOT NULL,
    created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    name                     VARCHAR(60)              NOT NULL,
    image_link               TEXT,
    priority                 INT,
    place_id                 BIGINT                   REFERENCES place(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (id)
);