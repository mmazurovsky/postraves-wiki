package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
import com.postraves.backend.postraveswiki.data.dto.writing.CityWriteDto
import com.postraves.backend.postraveswiki.repo.CityRepo
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

interface CityService :
    BaseService<CityWriteDto, CityDto>,
    ServiceByName<CityDto>

@Service
class CityServiceImpl(
    private val cityRepo: CityRepo,
    private val baseService: BaseService<CityWriteDto, CityDto> = BaseServiceImpl(cityRepo),
    private val serviceByName: ServiceByName<CityDto> = ServiceByNameImpl(cityRepo),
    ) : CityService {

    override fun findByName(name: String): CityDto {
        return serviceByName.findByName(name)
    }

    override fun save(dto: CityWriteDto):CityDto {
        return baseService.save(dto)
    }

    override fun update(dto: CityWriteDto) {
        baseService.update(dto)
    }

    override fun deleteByName(name: String) {
        serviceByName.deleteByName(name)
    }

    override fun findAll(): List<CityDto> {
        return baseService.findAll()
    }
}