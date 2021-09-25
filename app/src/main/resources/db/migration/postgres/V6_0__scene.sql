CREATE TABLE scene
(
    scene_id                       BIGSERIAL                UNIQUE NOT NULL,
    scene_created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    scene_name                     VARCHAR(60)              NOT NULL,
    scene_image_link               TEXT,
    scene_priority                 INT,
    scene_place_id                 BIGINT                   REFERENCES place(place_id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (scene_id)
);