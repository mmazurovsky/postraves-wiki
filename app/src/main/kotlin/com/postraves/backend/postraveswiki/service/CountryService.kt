package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.reading.CountryDto
import com.postraves.backend.postraveswiki.data.dto.writing.CountryWriteDto
import com.postraves.backend.postraveswiki.repo.CountryRepo
import org.springframework.stereotype.Service

interface CountryService :
    BaseService<CountryWriteDto, CountryDto>,
    ServiceByName<CountryDto>,
    FindByName<CountryDto>

@Service
class CountryServiceImpl(
    private val countryRepo: CountryRepo,
) : CountryService {

    override fun findByName(name: String): CountryDto {
        return countryRepo.findByName(name)
    }

    override fun save(dto: CountryWriteDto): CountryDto {
        return countryRepo.save(dto)
    }

    override fun saveBatch(list: List<CountryWriteDto>): List<CountryDto> {
        val saved = list.map {
            countryRepo.save(it)
        }.toList()

        return saved
    }

    override fun update(dto: CountryWriteDto) {
        countryRepo.update(dto)
    }

    override fun deleteByName(name: String) {
        countryRepo.deleteByName(name)
    }

    override fun findAll(): List<CountryDto> {
        return countryRepo.findAll()
    }

    override fun findByPartOfName(namePart: String): List<CountryDto> {
        return countryRepo.findByPartOfName(namePart)
    }
}