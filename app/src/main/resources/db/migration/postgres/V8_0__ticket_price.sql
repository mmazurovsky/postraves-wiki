CREATE TABLE ticket_price
(
    ticket_price_id                       BIGSERIAL                UNIQUE NOT NULL,
    ticket_price_created_date_time        TIMESTAMP                WITH TIME ZONE NOT NULL,
    ticket_price_name                     VARCHAR(60),
    ticket_price_price                    double precision         NOT NULL,
    ticket_price_currency                 text                     NOT NULL,
    ticket_price_event_id                 BIGINT                   REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (ticket_price_id)
);