package com.postraves.backend.postraveswiki.repo.followable

import com.postraves.backend.postraveswiki.data.dto.BaseWriteDto
import com.postraves.backend.postraveswiki.data.dto.FollowableFullDto
import com.postraves.backend.postraveswiki.data.dto.FollowableShortDto
import com.postraves.backend.postraveswiki.exception.NotFoundException
import com.postraves.backend.postraveswiki.repo.BaseRepo
import com.postraves.backend.postraveswiki.repo.ByIdRepo
import com.postraves.backend.postraveswiki.repo.FollowableRepo
import org.jooq.*
import org.jooq.impl.TableImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import java.time.OffsetDateTime

abstract class AbstractRepo<WRITEDTO : BaseWriteDto, FULLDTO : FollowableFullDto<FULLDTO>, SHORTDTO : FollowableShortDto<SHORTDTO>, R>(
    val table: TableImpl<R>,
    val entityType: String,
) : BaseRepo<WRITEDTO, SHORTDTO>,
    ByIdRepo<FULLDTO, SHORTDTO>,
    FollowableRepo<SHORTDTO>
        where R : Record,
              R : UpdatableRecord<R> {

    @Qualifier("getDSLContext")
    @Autowired
    @Lazy
    private lateinit var dsl: DSLContext

    abstract fun SelectJoinStep<Record>.joinLocation(): SelectOnConditionStep<Record>
    protected abstract fun SelectJoinStep<Record>.joinOtherData(): SelectOnConditionStep<Record>?
    protected abstract fun SelectJoinStep<Record>.joinUserFollow(userId: Long): SelectOnConditionStep<Record>
    protected abstract fun SelectWhereStep<Record>.whereMatchingId(id: Long): SelectConditionStep<Record>
    abstract override fun convertToShortDto(record: Record): SHORTDTO
    protected abstract fun convertToFullDto(record: Record): FULLDTO
    abstract fun SelectWhereStep<Record>.whereIdIsInIds(ids: Set<Long>): SelectConditionStep<Record>
    abstract fun SelectWhereStep<Record>.whereNameIsLikeAndOtherConditions(namePart: String): SelectConditionStep<Record>
    abstract fun prepareRecordBeforeSaving(record: R, dto: WRITEDTO)
    abstract fun postSaveGetId(record: R): Long
    abstract fun preUpdateCheckId(dto: WRITEDTO): Long
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

    private fun findByIdWithJoins(userId: Long?, id: Long): Record {
        val record =
            selectFromEntity()
                .joinLocation()
                .apply { if (joinOtherData() != null) joinOtherData() }
                .apply { if (userId != null) joinUserFollow(userId) }
                .whereMatchingId(id)
                .fetchOne()
        return record ?: throw NotFoundException(entityType, id.toString())
    }

    override fun findById(userId: Long?, id: Long): FULLDTO {
        val selectedRecord = findByIdWithJoins(userId, id)
        return convertToFullDto(selectedRecord)
    }

    override fun deleteById(id: Long) {
        findByIdWithoutJoins(id).delete()
    }

    override fun findListByIds(userId: Long?, ids: Set<Long>): List<SHORTDTO> {
        return selectFromEntity()
            .joinLocation()
            .apply { if (joinOtherData() != null) joinOtherData() }
            .apply { if (userId != null) joinUserFollow(userId) }
            .whereIdIsInIds(ids)
            .fetch()
            .map { convertToShortDto(it) }
            .toList()
    }

    override fun findAll(): List<SHORTDTO> {
        return selectFromEntity()
            .joinLocation()
            .apply { if (joinOtherData() != null) joinOtherData() }
            .orderBy(getUpdatedDateTimeOrderField())
            .fetch()
            .map {
                convertToShortDto(it)
            }
            .toList()
    }

    abstract fun getUpdatedDateTimeOrderField(): SortField<OffsetDateTime?>

    override fun findFollowableByPartOfName(userId: Long?, namePart: String): List<SHORTDTO> {
        return selectFromEntity()
            .joinLocation()
            .apply { if (joinOtherData() != null) joinOtherData() }
            .apply { if (userId != null) joinUserFollow(userId) }
            .whereNameIsLikeAndOtherConditions(namePart)
            .fetch()
            .map { convertToShortDto(it) }
            .toList()
    }

    override fun save(dto: WRITEDTO): SHORTDTO {
        val entityToSave = dsl.newRecord(table)
        prepareRecordBeforeSaving(entityToSave, dto)
        entityToSave.store()
        val savedId = postSaveGetId(entityToSave)
        postSaveProcessing(savedId, dto)
        val record = findByIdWithJoins(null, savedId)
        return convertToShortDto(record)
    }

    protected open fun postSaveProcessing(id: Long, dto: WRITEDTO) {
    }

    override fun update(dto: WRITEDTO) {
        val id = preUpdateCheckId(dto)
        val recordToUpdate = findByIdWithoutJoins(id)
        prepareRecordBeforeUpdating(recordToUpdate, dto)
        recordToUpdate.update()
        postUpdateProcessing(dto)
    }

    override fun updateUpdatedDateTime(id: Long) {
        val recordToUpdate = findByIdWithoutJoins(id)
        updateUpdatedDateTimeInRecord(recordToUpdate)
        recordToUpdate.update()
    }

    abstract fun updateUpdatedDateTimeInRecord(recordToUpdate: R)

    protected open fun postUpdateProcessing(dto: WRITEDTO) {
    }
}