package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.CityDto
import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.repo.CountryRepo
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.repo.CityRepo
import jooq.tables.records.CountryRecord
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service


interface CityService :
    BaseService<CityWriteDto, CityDto, CityDto>,
    ServiceByName<CityDto>

@Service
class CityServiceImpl(
    private val cityRepo: CityRepo,
    @Qualifier("baseServiceImpl")
    private val baseService: BaseService<CityWriteDto, CityDto, CityDto> = BaseServiceImpl(cityRepo),
    @Qualifier("serviceByNameImpl")
    private val serviceByName: ServiceByName<CityDto> = ServiceByNameImpl(cityRepo),
    ) : CityService {

    override fun findByName(name: String): CityDto {
        return serviceByName.findByName(name)
    }

    override fun save(dto: CityWriteDto): CityDto {
        return baseService.save(dto)
    }

    override fun update(dto: CityWriteDto): CityDto {
        return baseService.update(dto)
    }

    override fun deleteByName(name: String): CityDto {
        return serviceByName.deleteByName(name)
    }

    override fun findAll(): List<CityDto> {
        return baseService.findAll()
    }
}