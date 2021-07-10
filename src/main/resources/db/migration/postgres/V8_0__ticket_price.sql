CREATE TABLE ticket_price
(
    id                       BIGSERIAL                UNIQUE NOT NULL,
    created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    name                     VARCHAR(60),
    price                    double precision         NOT NULL,
    currency                 text                     NOT NULL,
    event_id                 BIGINT                   REFERENCES event(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (id)
);