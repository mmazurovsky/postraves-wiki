package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.ArtistDto
import com.postraves.backend.postraveswiki.data.projection.ArtistProjection
import jooq.tables.records.ArtistRecord
import jooq.tables.references.ARTIST
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.DriverManager

interface ArtistRepo : BaseOperationsRepo<ArtistDto, ArtistProjection> {
}

@Repository
class ArtistRepoImpl(
    @Value("\${spring.datasource.url}") private val url : String?,
    @Value("\${spring.datasource.username}") private val username : String?,
    @Value("\${spring.datasource.password}") private val password : String?
    ) : ArtistRepo {

    private val dialect: SQLDialect = SQLDialect.POSTGRES
    private val connection: Connection = DriverManager.getConnection(url, username, password)

    override fun findById(id: Long): ArtistProjection {
        val selectedRecord : ArtistRecord? = DSL.using(connection, dialect)
            .selectFrom(ARTIST)
            .where(ARTIST.ID.eq(id))
            .fetchOne()

        if (selectedRecord != null)
            return ArtistProjection(
                //TODO record id wont be null
            id = selectedRecord.id!!,
            name = selectedRecord.name!!,
            imageLink = selectedRecord.imageLink)
            else throw TODO()
    }

    override fun save(dto: ArtistDto): ArtistProjection? {
        TODO("Not yet implemented")
    }

    override fun update(dto: ArtistDto): ArtistProjection? {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<ArtistProjection> {
        TODO("Not yet implemented")
    }
}