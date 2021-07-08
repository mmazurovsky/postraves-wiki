CREATE TABLE timetable_item_performing_group
(
    timetable_item_id                      BIGINT                   REFERENCES timetable_item(id) ON UPDATE CASCADE ON DELETE CASCADE,
    artist_id                              BIGINT                   REFERENCES artist(id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (timetable_item_id, artist_id)
);