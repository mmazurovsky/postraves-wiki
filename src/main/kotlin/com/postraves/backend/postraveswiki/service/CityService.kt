package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.repo.CityRepo
import com.postraves.backend.postraveswiki.service.generic.BaseService
import com.postraves.backend.postraveswiki.service.generic.ServiceByName
import org.springframework.stereotype.Service

interface CityService :
    BaseService<CityWriteDto, CityDto>,
    ServiceByName<CityDto>

@Service
class CityServiceImpl(
    private val cityRepo: CityRepo,
    ) : CityService {

    override fun findByName(name: String): CityDto {
        return cityRepo.findByName(name)
    }

    override fun save(dto: CityWriteDto):CityDto {
        return cityRepo.save(dto)
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
}