package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.repo.FollowersQuickRepo
import com.postraves.backend.postraveswiki.repo.WeeklyBestRepo
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScheduledOperations(
    @Qualifier("artistWeeklyBestRepoImpl")
    private val artistWeeklyBestRepo: WeeklyBestRepo,
    @Qualifier("artistWeeklyFollowersQuickRepoImpl")
    private val artistWeeklyFollowersQuickRepo: FollowersQuickRepo,
    private val artistService: ArtistService,
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