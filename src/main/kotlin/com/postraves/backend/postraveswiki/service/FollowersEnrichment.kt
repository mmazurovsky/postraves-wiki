package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.BaseRatingDtoWithId
import com.postraves.backend.postraveswiki.repo.QuickFollowersRepoImpl

object FollowersEnrichment {
    fun <E, T : BaseRatingDtoWithId<E>> enrichWithFollowers(
        entity: T,
        overallRepo: QuickFollowersRepoImpl.OverallQuickFollowersRepo,
        weeklyRepo: QuickFollowersRepoImpl.WeeklyQuickFollowersDeltaRepo,
    ): E {
        val weeklyFollowers = weeklyRepo.getFollowers(entity.id)
        val overallFollowers = overallRepo.getFollowers(entity.id)
        return entity.copyWithFollowersEnriched(weeklyFollowers = weeklyFollowers, overallFollowers = overallFollowers)
    }
}
