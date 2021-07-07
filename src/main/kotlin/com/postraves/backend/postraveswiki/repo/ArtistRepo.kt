package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import jooq.tables.references.ARTIST
import jooq.tables.references.COUNTRY
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.DriverManager

interface ArtistRepo : BaseOperationsRepo<ArtistShortDto, ArtistFullDto> {
}

@Repository
class ArtistRepoImpl(
    @Value("\${spring.datasource.url}") private val url: String?,
    @Value("\${spring.datasource.username}") private val username: String?,
    @Value("\${spring.datasource.password}") private val password: String?
) : ArtistRepo {

    private val dialect: SQLDialect = SQLDialect.POSTGRES
    private val connection: Connection = DriverManager.getConnection(url, username, password)

    override fun findById(id: Long): ArtistFullDto {
        val selectedRecord: Record? = DSL.using(connection, dialect)
            .selectFrom(ARTIST.fullOuterJoin(COUNTRY).on(ARTIST.COUNTRY_ID.eq(COUNTRY.ID)))
            .where(ARTIST.ID.eq(id))
            .fetchOne()

        val artistRecord = selectedRecord?.into(ARTIST)
        val countryRecord = selectedRecord?.into(COUNTRY)

        return ArtistFullDto(
            //TODO record id wont be null
            id = artistRecord!!.id!!,
            name = artistRecord!!.name!!,
            imageLink = artistRecord!!.imageLink,
            rating = artistRecord!!.rating!!,
            instagramLink = artistRecord!!.instagramLink,
            soundcloudLink = artistRecord!!.soundcloudLink,
            about = artistRecord!!.about,
            country = CountryDto(
                name = countryRecord!!.name!!,
                phoneCode = countryRecord!!.phoneCode!!,
                emojiCode = countryRecord!!.emojiCode!!,
            )
        )
    }

    override fun save(dto: ArtistFullDto): ArtistFullDto? {
        TODO("Not yet implemented")
    }

    override fun update(dto: ArtistFullDto): ArtistFullDto? {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Long) : ArtistFullDto {
        TODO("Not yet implemented")
    }

    override fun findAll(): List<ArtistShortDto> {
        TODO("Not yet implemented")
    }
}