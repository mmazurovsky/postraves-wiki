package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.WeeklyBestQuickRepo
import com.postraves.backend.postraveswiki.service.followable.PlaceService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScheduledOperations(
    @Qualifier("artistWeeklyBestQuickRepoImpl")
    private val artistWeeklyBestRepo: WeeklyBestQuickRepo,
    @Qualifier("artistWeeklyFollowersQuickRepoImpl")
    private val artistWeeklyFollowersQuickRepo: FollowersQuickRepo,
    private val artistService: PlaceService,
) {

    @Scheduled(cron = "0 0 0 * * MON")
    fun setNewWeeklyBest() {
        artistService.setBestOfTheWeekForAllCities()
    }

    @Scheduled(cron = "0 0 5 * * MON")
    fun returnWeeklyRatingToInitial() {
        artistWeeklyFollowersQuickRepo.returnAllValuesToInitial()
    }
}