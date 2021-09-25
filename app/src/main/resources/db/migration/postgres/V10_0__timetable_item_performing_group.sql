CREATE TABLE timetable_item_performing_group
(
    timetable_item_performing_group_timetable_item_id                      BIGINT                   REFERENCES timetable_item(timetable_item_id) ON UPDATE CASCADE ON DELETE CASCADE,
    timetable_item_performing_group_artist_id                              BIGINT                   REFERENCES artist(artist_id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (timetable_item_performing_group_timetable_item_id, timetable_item_performing_group_artist_id)
);