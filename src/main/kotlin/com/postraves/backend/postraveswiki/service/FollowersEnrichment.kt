package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.BaseRatingDtoWithId
import com.postraves.backend.postraveswiki.repo.QuickFollowersRepo

object FollowersEnrichment {
    fun <E, T : BaseRatingDtoWithId<E>> enrichWithFollowers(
        entity: T,
        overallRepo: QuickFollowersRepo,
        weeklyRepo: QuickFollowersRepo,
    ): E {
        val weeklyFollowers = weeklyRepo.getFollowers(entity.id)
        val overallFollowers = overallRepo.getFollowers(entity.id)
        return entity.copyWithFollowersEnriched(weeklyFollowers = weeklyFollowers, overallFollowers = overallFollowers)
    }
}
