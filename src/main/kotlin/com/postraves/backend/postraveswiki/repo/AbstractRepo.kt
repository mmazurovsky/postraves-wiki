package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.BaseFullDtoWithId
import com.postraves.backend.postraveswiki.data.dto.BaseShortDtoWithId
import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.exception.UpdateException
import org.jooq.*
import org.jooq.impl.TableImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy

abstract class AbstractRepo<WRITEDTO : BaseWriteDto, FULLDTO : BaseFullDtoWithId, SHORTDTO : BaseShortDtoWithId, R>(
    val table: TableImpl<R>,
    val entityType: String,
) : BaseRepo<WRITEDTO, SHORTDTO>,
    ByIdRepo<FULLDTO, SHORTDTO>,
    FindByNameRepo<SHORTDTO>
        where R : Record,
              R : UpdatableRecord<R> {
    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    private fun selectFromEntity(): SelectJoinStep<Record> {
        return dsl
            .select()
            .from(table)
    }

    protected abstract fun SelectJoinStep<Record>.joinLocation(): SelectOnConditionStep<Record>

    protected abstract fun SelectJoinStep<Record>.joinUserFollow(authUid: String): SelectOnConditionStep<Record>

    protected abstract fun SelectWhereStep<Record>.whereMatchingId(id: Long): SelectConditionStep<Record>

    private fun Select<Record>.fetchOneEntity(): R? {
        return this.fetchOneInto(table)
    }

    protected fun findByIdWithoutJoins(id: Long): R {
        val record =
            selectFromEntity()
                .whereMatchingId(id)
                .fetchOneEntity()
        return record ?: throw NotFoundException(entityType, id.toString())
    }

    protected abstract fun convertToShortDto(record: Record): SHORTDTO

    protected abstract fun convertToFullDto(record: Record): FULLDTO

    protected fun findByIdWithJoins(id: Long): Record {
        val record =
            selectFromEntity()
                .joinLocation()
                .whereMatchingId(id)
                .fetchOne()
        return record ?: throw NotFoundException(entityType, id.toString())
    }

    private fun findByIdWithJoinsForUser(authUid: String, id: Long): Record {
        val record =
            selectFromEntity()
                .joinLocation()
                .joinUserFollow(authUid)
                .whereMatchingId(id)
                .fetchOne()
        return record ?: throw NotFoundException("Artist", id.toString())
    }

    override fun findById(authUid: String?, id: Long): FULLDTO {
        val selectedRecord = if (authUid == null) findByIdWithJoins(id) else findByIdWithJoinsForUser(authUid, id)
        return convertToFullDto(selectedRecord)
    }

    override fun deleteById(id: Long) {
        findByIdWithoutJoins(id).delete()
    }

    abstract fun SelectWhereStep<Record>.whereIdIsInIds(ids: Set<Long>): SelectConditionStep<Record>

    override fun findListByIds(ids: Set<Long>): List<SHORTDTO> {
        val results = selectFromEntity()
            .joinLocation()
            .whereIdIsInIds(ids)
            .fetch()
            .map { convertToShortDto(it) }
            .toList()
        return results
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

    abstract fun SelectWhereStep<Record>.whereNameIsLike(namePart: String): SelectConditionStep<Record>

    override fun findByPartOfName(namePart: String): List<SHORTDTO> {
        return selectFromEntity()
            .joinLocation()
            .whereNameIsLike(namePart)
            .fetch()
            .map { convertToShortDto(it) }
            .toList()
    }

    abstract fun prepareRecordBeforeSaving(record: R, dto: WRITEDTO)
    abstract fun postSaveGetId(record: R): Long

    override fun save(dto: WRITEDTO): SHORTDTO {
        val entityToSave = dsl.newRecord(table)
        prepareRecordBeforeSaving(entityToSave, dto)

        entityToSave.store()
        val savedId = postSaveGetId(entityToSave)
        val record = findByIdWithJoins(savedId)
        return convertToShortDto(record)
    }

    abstract fun preUpdateGetId(dto: WRITEDTO): Long
    abstract fun prepareRecordBeforeUpdating(record: R, dto: WRITEDTO)

    override fun update(dto: WRITEDTO) {
        val id = preUpdateGetId(dto)
        val recordToUpdate = findByIdWithoutJoins(id)
        prepareRecordBeforeUpdating(recordToUpdate, dto)
        recordToUpdate.update()
    }
}