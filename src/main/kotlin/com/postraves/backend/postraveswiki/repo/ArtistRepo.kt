package com.postraves.backend.postraveswiki.repo

import com.postraves.backend.postraveswiki.data.dto.ArtistDto
import com.postraves.backend.postraveswiki.data.projection.ArtistProjection
import org.springframework.stereotype.Repository

interface ArtistRepo : BaseOperationsRepo<ArtistDto, ArtistProjection> {
}

@Repository
class ArtistRepoImpl : ArtistRepo {

    override fun findById(id: Long): ArtistProjection {
        TODO()
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