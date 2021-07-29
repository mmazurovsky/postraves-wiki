package com.postraves.backend.postraveswiki.repo.followable

import com.postraves.backend.postraveswiki.data.dto.BaseFullDtoWithId
import com.postraves.backend.postraveswiki.data.dto.BaseShortDtoWithId
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.repo.BaseRepo
import com.postraves.backend.postraveswiki.repo.ByIdRepo
import com.postraves.backend.postraveswiki.repo.FollowableRepo
import org.jooq.*
import org.jooq.impl.TableImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy

abstract class AbstractRepo<WRITEDTO : BaseWriteDto, FULLDTO : BaseFullDtoWithId, SHORTDTO : BaseShortDtoWithId, R>(
    val table: TableImpl<R>,
    val entityType: String,
) : BaseRepo<WRITEDTO, SHORTDTO>,
    ByIdRepo<FULLDTO, SHORTDTO>,
    FollowableRepo<SHORTDTO>
        where R : Record,
              R : UpdatableRecord<R> {
    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    protected abstract fun SelectJoinStep<Record>.joinLocation(): SelectOnConditionStep<Record>
    protected abstract fun SelectJoinStep<Record>.joinUserFollow(authUid: String): SelectOnConditionStep<Record>
    protected abstract fun SelectWhereStep<Record>.whereMatchingId(id: Long): SelectConditionStep<Record>
    protected abstract fun convertToShortDto(record: Record): SHORTDTO
    protected abstract fun convertToFullDto(record: Record): FULLDTO
    abstract fun SelectWhereStep<Record>.whereIdIsInIds(ids: Set<Long>): SelectConditionStep<Record>
    abstract fun SelectWhereStep<Record>.whereNameIsLike(namePart: String): SelectConditionStep<Record>
    abstract fun prepareRecordBeforeSaving(record: R, dto: WRITEDTO)
    abstract fun postSaveGetId(record: R): Long
    abstract fun preUpdateGetId(dto: WRITEDTO): Long
    abstract fun prepareRecordBeforeUpdating(record: R, dto: WRITEDTO)

    private fun selectFromEntity(): SelectJoinStep<Record> {
        return dsl
            .select()
            .from(table)
    }

    private fun Select<Record>.fetchOneEntity(): R? {
        return this.fetchOneInto(table)
    }

    private fun findByIdWithoutJoins(id: Long): R {
        val record =
            selectFromEntity()
                .whereMatchingId(id)
                .fetchOneEntity()
        return record ?: throw NotFoundException(entityType, id.toString())
    }

    private fun findByIdWithJoins(authUid: String?, id: Long): Record {
        val record =
            selectFromEntity()
                .joinLocation()
                .apply {if (authUid != null) joinUserFollow(authUid)}
                .whereMatchingId(id)
                .fetchOne()
        return record ?: throw NotFoundException(entityType, id.toString())
    }

    override fun findById(authUid: String?, id: Long): FULLDTO {
        val selectedRecord = findByIdWithJoins(authUid, id)
        return convertToFullDto(selectedRecord)
    }

    override fun deleteById(id: Long) {
        findByIdWithoutJoins(id).delete()
    }

    override fun findListByIds(authUid: String?, ids: Set<Long>): List<SHORTDTO> {
        return selectFromEntity()
            .joinLocation()
            .apply {if (authUid != null) joinUserFollow(authUid)}
            .whereIdIsInIds(ids)
            .fetch()
            .map { convertToShortDto(it) }
            .toList()
    }

    override fun findAll(): List<SHORTDTO> {
        return selectFromEntity()
            .joinLocation()
            .fetch()
            .map {
                convertToShortDto(it)
            }
            .toList()
    }

    override fun findFollowableByPartOfName(authUid: String?, namePart: String): List<SHORTDTO> {
        return selectFromEntity()
            .joinLocation()
            .apply {if (authUid != null) joinUserFollow(authUid)}
            .whereNameIsLike(namePart)
            .fetch()
            .map { convertToShortDto(it) }
            .toList()
    }

    override fun save(dto: WRITEDTO): SHORTDTO {
        val entityToSave = dsl.newRecord(table)
        prepareRecordBeforeSaving(entityToSave, dto)
        entityToSave.store()
        val savedId = postSaveGetId(entityToSave)
        val record = findByIdWithJoins(null, savedId)
        return convertToShortDto(record)
    }

    override fun update(dto: WRITEDTO) {
        val id = preUpdateGetId(dto)
        val recordToUpdate = findByIdWithoutJoins(id)
        prepareRecordBeforeUpdating(recordToUpdate, dto)
        recordToUpdate.update()
    }
}