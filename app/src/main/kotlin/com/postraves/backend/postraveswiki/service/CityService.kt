package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.repo.CityRepo
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable

interface CityService :
    BaseService<CityWriteDto, CityDto>,
    ServiceByName<CityDto>,
    FindByName<CityDto> {
    fun findAllInternal(): List<CityWriteDto>
    fun findByNameInternal(name: String): CityWriteDto
}

@Service
class CityServiceImpl(
    private val cityRepo: CityRepo,
) : CityService {

    override fun findByName(name: String): CityDto {
        return cityRepo.findByName(name)
    }

    override fun findAllInternal(): List<CityWriteDto> {
        return cityRepo.findAllInternal()
    }

    override fun findByNameInternal(name: String): CityWriteDto {
        return cityRepo.findByNameInternal(name)
    }

    override fun save(dto: CityWriteDto): CityDto {
        return cityRepo.save(dto)
    }

    override fun saveBatch(list: List<CityWriteDto>): List<CityDto> {
        return list.map {
            cityRepo.save(it)
        }.toList()
    }

    override fun update(dto: CityWriteDto) {
        cityRepo.update(dto)
    }

    override fun deleteByName(name: String) {
        cityRepo.deleteByName(name)
    }

    override fun findAll(): List<CityDto> {
        return cityRepo.findAll()
    }

    override fun findByPartOfName(namePart: String): List<CityDto> {
        return cityRepo.findByPartOfName(namePart)
    }
}