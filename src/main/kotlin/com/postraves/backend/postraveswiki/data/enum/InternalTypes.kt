package com.postraves.backend.postraveswiki.data.enum

enum class EntityType(val nameString: String) {
    ARTIST(nameString = "artist"),
    UNITY(nameString = "unity"),
}

enum class FollowersType(val nameString: String) {
    WEEKLY(nameString = "weeklyFollowers"),
    OVERALL(nameString = "overallFollowers"),
}
