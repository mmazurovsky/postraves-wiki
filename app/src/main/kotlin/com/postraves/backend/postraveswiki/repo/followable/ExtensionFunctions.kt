package com.postraves.backend.postraveswiki.repo.followable

import jooq.tables.references.*
import org.jooq.Record
import org.jooq.SelectJoinStep
import org.jooq.SelectOnConditionStep

fun SelectJoinStep<Record>.joinArtistLocation(): SelectOnConditionStep<Record> {
    return this.leftOuterJoin(COUNTRY).on(ARTIST.ARTIST_COUNTRY_NAME.eq(COUNTRY.COUNTRY_NAME))
}

fun SelectJoinStep<Record>.joinArtistUserFollow(userId: Long): SelectOnConditionStep<Record> {
    return this.leftOuterJoin(USER_FOLLOWS_ARTIST)
            .on(
                    ARTIST.ARTIST_ID.eq(USER_FOLLOWS_ARTIST.USER_FOLLOWS_ARTIST_ARTIST_ID),
                    USER_FOLLOWS_ARTIST.USER_FOLLOWS_ARTIST_USER_PROFILE_ID.eq(userId)
            )
}

fun SelectJoinStep<Record>.joinEventLocation(): SelectOnConditionStep<Record> {
    return this.leftOuterJoin(PLACE)
            .on(EVENT.EVENT_PLACE_ID.eq(PLACE.PLACE_ID))
            .leftOuterJoin(CITY)
            .on(PLACE.PLACE_CITY_NAME.eq(CITY.CITY_NAME))
            .leftOuterJoin(COUNTRY)
            .on(CITY.CITY_COUNTRY_NAME.eq(COUNTRY.COUNTRY_NAME))
}

fun SelectJoinStep<Record>.joinEventUserFollow(userId: Long): SelectOnConditionStep<Record> {
    return this.leftOuterJoin(USER_FOLLOWS_EVENT)
            .on(
                    EVENT.EVENT_ID.eq(USER_FOLLOWS_EVENT.USER_FOLLOWS_EVENT_EVENT_ID),
                    USER_FOLLOWS_EVENT.USER_FOLLOWS_EVENT_USER_PROFILE_ID.eq(userId)
            )
            .leftOuterJoin(USER_FOLLOWS_PLACE)
            .on(
                    PLACE.PLACE_ID.eq(USER_FOLLOWS_PLACE.USER_FOLLOWS_PLACE_PLACE_ID),
                    USER_FOLLOWS_PLACE.USER_FOLLOWS_PLACE_USER_PROFILE_ID.eq(userId)
            )
}

fun SelectJoinStep<Record>.joinPlaceLocation(): SelectOnConditionStep<Record> {
    return this.leftOuterJoin(CITY)
            .on(PLACE.PLACE_CITY_NAME.eq(CITY.CITY_NAME))
            .leftOuterJoin(COUNTRY)
            .on(CITY.CITY_COUNTRY_NAME.eq(COUNTRY.COUNTRY_NAME))
}

fun SelectJoinStep<Record>.joinPlaceUserFollow(userId: Long): SelectOnConditionStep<Record> {
    return this.leftOuterJoin(USER_FOLLOWS_PLACE)
            .on(
                    PLACE.PLACE_ID.eq(USER_FOLLOWS_PLACE.USER_FOLLOWS_PLACE_PLACE_ID),
                    USER_FOLLOWS_PLACE.USER_FOLLOWS_PLACE_USER_PROFILE_ID.eq(userId)
            )
}

fun SelectJoinStep<Record>.joinUnityLocation(): SelectOnConditionStep<Record> {
    return this.leftOuterJoin(COUNTRY).on(UNITY.UNITY_COUNTRY_NAME.eq(COUNTRY.COUNTRY_NAME))
}

fun SelectJoinStep<Record>.joinUnityUserFollow(userId: Long): SelectOnConditionStep<Record> {
    return this.leftOuterJoin(USER_FOLLOWS_UNITY)
            .on(
                    UNITY.UNITY_ID.eq(USER_FOLLOWS_UNITY.USER_FOLLOWS_UNITY_UNITY_ID),
                    USER_FOLLOWS_UNITY.USER_FOLLOWS_UNITY_USER_PROFILE_ID.eq(userId)
            )
}
