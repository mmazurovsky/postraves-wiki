package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.CountryDto
import com.postraves.backend.postraveswiki.repo.CountryRepo
import org.springframework.stereotype.Service

interface CountryService :
    BaseService<CountryDto, CountryDto>,
    ServiceByName<CountryDto>

@Service
class CountryServiceImpl(
    private val countryRepo: CountryRepo,
    private val baseService: BaseService<CountryDto, CountryDto> = BaseServiceImpl(countryRepo),
    private val serviceByName: ServiceByName<CountryDto> = ServiceByNameImpl(countryRepo),
    ) : CountryService {

    override fun findByName(name: String): CountryDto {
        return serviceByName.findByName(name)
    }

    override fun save(dto: CountryDto):CountryDto {
        return baseService.save(dto)
    }

    override fun update(dto: CountryDto) {
        baseService.update(dto)
    }

    override fun deleteByName(name: String) {
        serviceByName.deleteByName(name)
    }

    override fun findAll(): List<CountryDto> {
        return baseService.findAll()
    }
}