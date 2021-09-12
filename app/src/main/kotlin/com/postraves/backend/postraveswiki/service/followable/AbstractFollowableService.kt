package com.postraves.backend.postraveswiki.service.followable

import com.postraves.backend.postraveswiki.data.dto.*
import com.postraves.backend.postraveswiki.repo.BaseRepo
import com.postraves.backend.postraveswiki.repo.ByIdRepo
import com.postraves.backend.postraveswiki.repo.FollowableRepo
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.service.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy

abstract class AbstractFollowableService<WRITEDTO : BaseWriteDto,
        FULLDTO : FollowableFullDto<FULLDTO>,
        SHORTDTO : FollowableShortDto<SHORTDTO>,
        REPO>
    (
    private val entityWeeklyFollowersQuickRepo: FollowersQuickRepo,
    private val entityOverallFollowersQuickRepo: FollowersQuickRepo,
    private val entityRepo: REPO,
) : BaseService<WRITEDTO, SHORTDTO>, FindByName<SHORTDTO>, ByIdService<FULLDTO, SHORTDTO>,
    FollowableService<FULLDTO, SHORTDTO>
        where REPO : BaseRepo<WRITEDTO, SHORTDTO>,
              REPO : ByIdRepo<FULLDTO, SHORTDTO>,
              REPO : FollowableRepo<SHORTDTO> {

    @Autowired
    @Lazy
    private lateinit var myUserProfileService: MyUserProfileService

    private fun findByIdDependingOnUser(id: Long): FULLDTO {
        val authUid = myUserProfileService.getMyAuthUidOnlyIfUserProfileExists()
        return entityRepo.findById(authUid, id)
    }

    override fun findById(id: Long): FULLDTO {
        val foundArtist = findByIdDependingOnUser(id)
        return enrichWithFollowersCalculationRequired(foundArtist)
    }

    private fun calculateFollowers(id: Long): Pair<Int, Int> {
        val overallFollowers = entityOverallFollowersQuickRepo.getFollowers(id)
        val weeklyFollowers = entityWeeklyFollowersQuickRepo.getFollowers(id)
        return overallFollowers to weeklyFollowers
    }

    fun <T : FollowableDto<T>> enrichWithFollowersCalculationRequired(dto: T): T {
        val followers = calculateFollowers(dto.id)
        return dto.copyWithFollowersEnriched(followers.first, followers.second)
    }

    override fun deleteById(id: Long) {
        // deleting form quick repo country
        val dtoToDelete = entityRepo.findById(null, id)

        checkLocationsAndRemoveFromLocationsQuickRepos(dtoToDelete)

        // deleting form quick repos ratings
        entityOverallFollowersQuickRepo.removeId(id)
        entityWeeklyFollowersQuickRepo.removeId(id)

        entityRepo.deleteById(id)
    }

    abstract fun checkLocationsAndRemoveFromLocationsQuickRepos(dto: FULLDTO)

    override fun save(dto: WRITEDTO): SHORTDTO {
        preProcessBeforeSaving(dto)
        val saved = entityRepo.save(dto)
        checkLocationsAndAddToLocationsQuickRepos(dto, saved.id)
        entityOverallFollowersQuickRepo.setInitialFollowers(saved.id)
        entityWeeklyFollowersQuickRepo.setInitialFollowers(saved.id)
        return saved
    }

    open fun preProcessBeforeSaving(dto: WRITEDTO) {}

    override fun saveBatch(list: List<WRITEDTO>): List<SHORTDTO> {
        return list.map {
            entityRepo.save(it)
        }.toList()
    }

    abstract fun checkLocationsAndAddToLocationsQuickRepos(dto: WRITEDTO, id: Long)

    override fun update(dto: WRITEDTO) {
        // check country change and delete+add if necessary
        checkLocationsAndAddAndRemoveFromLocationsQuickRepos(dto)
        entityRepo.update(dto)
    }

    abstract fun checkLocationsAndAddAndRemoveFromLocationsQuickRepos(dto: WRITEDTO)

    override fun incrementFollowers(id: Long) {
        if (myUserProfileService.getMyAuthUidOnlyIfUserProfileExists() != null) {
            entityOverallFollowersQuickRepo.incrementFollowers(id)
            entityWeeklyFollowersQuickRepo.incrementFollowers(id)
        } else throw TODO()
    }

    override fun decrementFollowers(id: Long) {
        if (myUserProfileService.getMyAuthUidOnlyIfUserProfileExists() != null) {
            entityOverallFollowersQuickRepo.decrementFollowers(id)
            entityWeeklyFollowersQuickRepo.decrementFollowers(id)
        }
    }

    override fun findAll(): List<SHORTDTO> {
        return entityRepo.findAll()
    }

    // todo not enriched with rating
    override fun findByPartOfName(namePart: String): List<SHORTDTO> {
        val authUid = myUserProfileService.getMyAuthUidOnlyIfUserProfileExists()
        return entityRepo.findFollowableByPartOfName(authUid, namePart)
    }

    override fun enrichListWithFollowersAndSortByOverallFollowers(list: List<SHORTDTO>): List<SHORTDTO> {
        return list.map { this.enrichWithFollowersCalculationRequired(it) }
            .sortedByDescending { it.overallFollowers }
            .toList()
    }
}
