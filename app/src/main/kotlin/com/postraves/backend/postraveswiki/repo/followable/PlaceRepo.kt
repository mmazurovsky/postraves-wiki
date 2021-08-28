package com.postraves.backend.postraveswiki.repo.followable

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
            .leftOuterJoin(CITY).on(PLACE.CITY_NAME.eq(CITY.NAME))
            .leftOuterJoin(COUNTRY).on(CITY.COUNTRY_NAME.eq(COUNTRY.NAME))
    }

    override fun SelectJoinStep<Record>.joinUserFollow(authUid: String): SelectOnConditionStep<Record> {
        return this.leftOuterJoin(USER_FOLLOWS_PLACE)
            .on(PLACE.ID.eq(USER_FOLLOWS_PLACE.PLACE_ID), USER_FOLLOWS_PLACE.USER_PROFILE_UID.eq(authUid))
    }

    override fun SelectJoinStep<Record>.joinOtherData(): SelectOnConditionStep<Record>? {
        return null
    }

    override fun SelectWhereStep<Record>.whereMatchingId(id: Long): SelectConditionStep<Record> {
        return this.where(PLACE.ID.eq(id))
    }

    override fun convertToShortDto(record: Record): PlaceShortDto {
        val isFollowed = record.into(USER_FOLLOWS_PLACE).userProfileUid != null
        return PlaceShortDto.createOutOfDbRecords(
            record.into(PLACE),
            record.into(CITY),
            record.into(COUNTRY),
            isFollowed
        )
    }

    override fun convertToFullDto(record: Record): PlaceFullDto {
        val isFollowed = record.into(USER_FOLLOWS_PLACE).userProfileUid != null
        return PlaceFullDto.createOutOfDbRecords(
            record.into(PLACE),
            record.into(CITY),
            record.into(COUNTRY),
            isFollowed
        )
    }

    override fun SelectWhereStep<Record>.whereIdIsInIds(ids: Set<Long>): SelectConditionStep<Record> {
        return this.where(PLACE.ID.`in`(ids))
    }

    override fun SelectWhereStep<Record>.whereNameIsLike(namePart: String): SelectConditionStep<Record> {
        return this.where(lower(PLACE.NAME).contains(namePart.lowercase()))
    }

    override fun prepareRecordBeforeSaving(record: PlaceRecord, dto: PlaceWriteDto) {
        dto.transferDataToDbRecord(record)
        record.createdDateTime = dateTimeProvider.getNow()
    }

    override fun postSaveGetId(record: PlaceRecord): Long {
        return record.id ?: throw SaveException("Place", record.name ?: "NULL")
    }

    override fun preUpdateGetId(dto: PlaceWriteDto): Long {
        return dto.id ?: throw NotFoundException("Place", dto.id.toString())
    }

    override fun prepareRecordBeforeUpdating(record: PlaceRecord, dto: PlaceWriteDto) {
        dto.transferDataToDbRecord(record)
    }

    override fun getAllScenes(): List<SceneDto> {
        val scenes = dsl
            .selectFrom(SCENE)
            .orderBy(SCENE.PRIORITY.desc())
            .fetch()
            .map { SceneDto.createOutOfDbRecords(it) }
            .toList()
        return scenes
    }

    override fun getScenesOfPlace(id: Long): List<SceneDto> {
        val scenes = dsl
            .selectFrom(SCENE)
            .where(SCENE.PLACE_ID.eq(id))
            .orderBy(SCENE.PRIORITY.desc())
            .fetch()
            .map { SceneDto.createOutOfDbRecords(it) }
            .toList()
        return scenes
    }

    override fun addScenesToPlace(id: Long, scenes: Set<SceneDto>) {
        scenes.forEach {
            val newSceneRecord = dsl.newRecord(SCENE)
            it.transferDataToDbRecord(newSceneRecord)
            newSceneRecord.createdDateTime = dateTimeProvider.getNow()
            newSceneRecord.placeId = id
            newSceneRecord.store()
        }
    }

    override fun updateScenes(scenes: Set<SceneDto>) {
        scenes.forEach {
            val recordToUpdate = dsl.fetchOne(SCENE, SCENE.ID.eq(it.id))!!
            it.transferDataToDbRecord(recordToUpdate)
            recordToUpdate.update()
        }
    }

    override fun removeScenes(scenes: Set<SceneDto>) {
        scenes.forEach {
            dsl
                .delete(SCENE)
                .where(SCENE.ID.eq(it.id))
                .execute()
        }
    }
}
