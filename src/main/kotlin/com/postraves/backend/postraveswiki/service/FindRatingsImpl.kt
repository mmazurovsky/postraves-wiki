package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.BaseFullDtoWithId
import com.postraves.backend.postraveswiki.data.dto.BaseShortDtoWithId
import com.postraves.backend.postraveswiki.repo.QuickEntityCountryRepo
import com.postraves.backend.postraveswiki.repo.QuickFollowersRepo
import java.util.*
import kotlin.math.min

class FindRatingsImpl<FULLDTO : BaseFullDtoWithId, SHORTDTO : BaseShortDtoWithId, SERVICE : ByIdService<FULLDTO, SHORTDTO>>(
    private val cityService: CityService,
    private val byIdService: SERVICE,
    private val entityCountryRepo: QuickEntityCountryRepo,
    private val overallRepo: QuickFollowersRepo,
    private val weeklyRepo: QuickFollowersRepo,
    private val copyFunction: (SHORTDTO, Int, Int) -> SHORTDTO
) {

    fun findOverallRatingForCityByCountry(cityName: String, maxQuantity: Int): List<SHORTDTO> {
        val countryName = cityService.findByName(cityName).country.name
        val artistFromTheCountryIds = entityCountryRepo.getAllIdsByCountry(countryName)
        val topArtistIdsAndScores = overallRepo.findTop(-1)
        val topArtistFromTheCountryIdsAndScores =
            topArtistIdsAndScores.filterKeys { artistFromTheCountryIds.contains(it) }.toMap()
        // todo maybe order gets lost here
        val topArtistFromTheCountryIds = topArtistFromTheCountryIdsAndScores.keys
        val topArtistFromTheCountryDtos = byIdService.findListByIds(topArtistFromTheCountryIds)
        val topArtistFromTheCountryDtosWithOverallFollowers = topArtistFromTheCountryDtos.map {
            copyFunction(it, topArtistFromTheCountryIdsAndScores[it.id] ?: throw TODO(), weeklyRepo.getFollowers(it.id))
        }.toList()
        Collections.sort(
            topArtistFromTheCountryDtosWithOverallFollowers,
            Comparator.comparing { topArtistFromTheCountryIds.indexOf(it.id) })
        val result = topArtistFromTheCountryDtosWithOverallFollowers.subList(
            0,
            min(topArtistFromTheCountryDtosWithOverallFollowers.size, maxQuantity)
        )
        return result
    }
}