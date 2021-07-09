package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.repo.CountryRepo
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistFullDto
import com.postraves.backend.postraveswiki.data.dto.reading.ArtistShortDto
import com.postraves.backend.postraveswiki.data.dto.writing.ArtistWriteDto
import jooq.tables.records.CountryRecord
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service


interface CountryService :
    BaseService<CountryDto, CountryDto, CountryDto>,
    ServiceByName<CountryDto>

@Service
class CountryServiceImpl(
    private val countryRepo: CountryRepo,
    @Qualifier("baseServiceImpl")
    private val baseService: BaseService<CountryDto, CountryDto, CountryDto> = BaseServiceImpl(countryRepo),
    @Qualifier("serviceByNameImpl")
    private val serviceByName: ServiceByName<CountryDto> = ServiceByNameImpl(countryRepo),
    ) : CountryService {

    override fun findByName(name: String): CountryDto {
        return serviceByName.findByName(name)
    }

    override fun save(dto: CountryDto): CountryDto {
        return baseService.save(dto)
    }

    override fun update(dto: CountryDto): CountryDto {
        return baseService.update(dto)
    }

    override fun deleteByName(name: String): CountryDto {
        return serviceByName.deleteByName(name)
    }

    override fun findAll(): List<CountryDto> {
        return baseService.findAll()
    }
}