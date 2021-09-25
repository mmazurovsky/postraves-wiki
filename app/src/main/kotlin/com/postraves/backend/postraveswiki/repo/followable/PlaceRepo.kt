package com.postraves.backend.postraveswiki.repo.followable

import com.postraves.backend.postraveswiki.data.converters.PlaceConverters
import com.postraves.backend.postraveswiki.data.converters.SceneConverters
import com.postraves.backend.postraveswiki.data.dto.reading.PlaceFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.PlaceShortDto
import com.postraves.backend.postraveswiki.data.dto.reading.SceneDto
import com.postraves.backend.postraveswiki.data.dto.writing.PlaceWriteDto
import com.postraves.backend.postraveswiki.data.enum.EntityType
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.exception.SaveException
import com.postraves.backend.postraveswiki.repo.BaseRepo
import com.postraves.backend.postraveswiki.repo.ByIdRepo
import com.postraves.backend.postraveswiki.repo.FollowableRepo
import com.postraves.backend.postraveswiki.util.DateTimeProvider
import jooq.tables.records.PlaceRecord
import jooq.tables.references.*
import org.jooq.*
import org.jooq.impl.DSL.lower
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime


interface PlaceRepo :
    BaseRepo<PlaceWriteDto, PlaceShortDto>,
    ByIdRepo<PlaceFullDto, PlaceShortDto>,
    FollowableRepo<PlaceShortDto> {
        fun getAllScenes(): List<SceneDto>
        fun getScenesOfPlace(id: Long): List<SceneDto>
        fun addScenesToPlace(id: Long, scenes: Set<SceneDto>)
        fun updateScenes(scenes: Set<SceneDto>)
        fun removeScenes(scenes: Set<SceneDto>)
}

@Repository
class PlaceRepoImpl(
    private val placeConverters: PlaceConverters,
    private val sceneConverters: SceneConverters,
    private val dateTimeProvider: DateTimeProvider,
    ) :
    PlaceRepo,
    AbstractRepo<PlaceWriteDto, PlaceFullDto, PlaceShortDto, PlaceRecord>(
        table = PLACE,
        entityType = EntityType.PLACE.nameString
    ) {

    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    override fun SelectJoinStep<Record>.joinLocation(): SelectOnConditionStep<Record> {
        return this
            .leftOuterJoin(CITY).on(PLACE.PLACE_CITY_NAME.eq(CITY.CITY_NAME))
            .leftOuterJoin(COUNTRY).on(CITY.CITY_COUNTRY_NAME.eq(COUNTRY.COUNTRY_NAME))
    }

    override fun SelectJoinStep<Record>.joinUserFollow(authUid: String): SelectOnConditionStep<Record> {
        return this.leftOuterJoin(USER_FOLLOWS_PLACE)
            .on(PLACE.PLACE_ID.eq(USER_FOLLOWS_PLACE.USER_FOLLOWS_PLACE_PLACE_ID), USER_FOLLOWS_PLACE.USER_FOLLOWS_PLACE_USER_PROFILE_UID.eq(authUid))
    }

    override fun SelectJoinStep<Record>.joinOtherData(): SelectOnConditionStep<Record>? {
        return null
    }

    override fun SelectWhereStep<Record>.whereMatchingId(id: Long): SelectConditionStep<Record> {
        return this.where(PLACE.PLACE_ID.eq(id))
    }

    override fun convertToShortDto(record: Record): PlaceShortDto {
        val isFollowed = record.into(USER_FOLLOWS_PLACE).userFollowsPlaceUserProfileUid != null
        return placeConverters.createShortDtoFromRecord(
            record.into(PLACE),
            record.into(CITY),
            record.into(COUNTRY),
            isFollowed
        )
    }

    override fun convertToFullDto(record: Record): PlaceFullDto {
        val isFollowed = record.into(USER_FOLLOWS_PLACE).userFollowsPlaceUserProfileUid != null
        return placeConverters.createFullDtoFromRecord(
            record.into(PLACE),
            record.into(CITY),
            record.into(COUNTRY),
            isFollowed
        )
    }

    override fun SelectWhereStep<Record>.whereIdIsInIds(ids: Set<Long>): SelectConditionStep<Record> {
        return this.where(PLACE.PLACE_ID.`in`(ids))
    }

    override fun SelectWhereStep<Record>.whereNameIsLike(namePart: String): SelectConditionStep<Record> {
        return this.where(lower(PLACE.PLACE_NAME).contains(namePart.lowercase()))
    }

    override fun prepareRecordBeforeSaving(record: PlaceRecord, dto: PlaceWriteDto) {
        placeConverters.transferDataFromDtoToRecord(dto, record)
        record.placeCreatedDateTime = dateTimeProvider.getNow()
    }

    override fun postSaveGetId(record: PlaceRecord): Long {
        return record.placeId ?: throw SaveException("Place", record.placeName ?: "NULL")
    }

    override fun preUpdateGetId(dto: PlaceWriteDto): Long {
        return dto.id ?: throw NotFoundException("Place", dto.id.toString())
    }

    override fun prepareRecordBeforeUpdating(record: PlaceRecord, dto: PlaceWriteDto) {
        placeConverters.transferDataFromDtoToRecord(dto, record)
    }

    override fun getAllScenes(): List<SceneDto> {
        val scenes = dsl
            .selectFrom(SCENE)
            .orderBy(SCENE.SCENE_PRIORITY.desc())
            .fetch()
            .map { sceneConverters.createDtoFromRecord(it) }
            .toList()
        return scenes
    }

    override fun getScenesOfPlace(id: Long): List<SceneDto> {
        val scenes = dsl
            .selectFrom(SCENE)
            .where(SCENE.SCENE_PLACE_ID.eq(id))
            .orderBy(SCENE.SCENE_PRIORITY.desc())
            .fetch()
            .map { sceneConverters.createDtoFromRecord(it) }
            .toList()
        return scenes
    }

    override fun addScenesToPlace(id: Long, scenes: Set<SceneDto>) {
        scenes.forEach {
            val newSceneRecord = dsl.newRecord(SCENE)
            sceneConverters.transferDataFromDtoToRecord(it, newSceneRecord)
            newSceneRecord.sceneCreatedDateTime = dateTimeProvider.getNow()
            newSceneRecord.scenePlaceId = id
            newSceneRecord.store()
        }
    }

    override fun updateScenes(scenes: Set<SceneDto>) {
        scenes.forEach {
            val recordToUpdate = dsl.fetchOne(SCENE, SCENE.SCENE_ID.eq(it.id))!!
            sceneConverters.transferDataFromDtoToRecord(it, recordToUpdate)
            recordToUpdate.update()
        }
    }

    override fun removeScenes(scenes: Set<SceneDto>) {
        scenes.forEach {
            dsl
                .delete(SCENE)
                .where(SCENE.SCENE_ID.eq(it.id))
                .execute()
        }
    }
}
