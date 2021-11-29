CREATE TABLE brand_advertisement
(
    brand_advertisement_id              BIGSERIAL UNIQUE         NOT NULL,
    brand_advertisement_brand_name      VARCHAR(60)              NOT NULL,
    brand_advertisement_image_link      TEXT,
    brand_advertisement_text            TEXT,
    brand_advertisement_action_message  TEXT,
    brand_advertisement_link            TEXT,
    brand_advertisement_created_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    brand_advertisement_updated_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    brand_advertisement_start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    brand_advertisement_end_date_time   TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (brand_advertisement_id)
);