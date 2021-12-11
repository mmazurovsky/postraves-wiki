package com.postraves.backend.postraveswiki.data.enum

enum class EntityType(val nameString: String) {
    ARTIST(nameString = "artist"),
    UNITY(nameString = "unity"),
    PLACE(nameString = "place"),
    EVENT(nameString = "event"),
}

enum class FollowersType(val nameString: String) {
    WEEKLY(nameString = "weeklyFollowers"),
    OVERALL(nameString = "overallFollowers"),
}

enum class EventStatus {
    UPCOMING,
    PRESALE,
    TOMORROW,
    TODAY,
    LIVE,
    PAST,
    CANCELLED
}

enum class UserProfileRole {
    USER,
    ADMIN
}