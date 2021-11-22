package com.postraves.backend.postraveswiki.service

import com.postraves.backend.postraveswiki.data.dto.MoneyCurrencyDto
import com.postraves.backend.postraveswiki.repo.MoneyCurrencyRepo
import org.springframework.stereotype.Service

interface MoneyCurrencyService :
    BaseService<MoneyCurrencyDto, MoneyCurrencyDto>,
    ServiceByName<MoneyCurrencyDto>,
    FindByName<MoneyCurrencyDto>

@Service
class MoneyCurrencyServiceImpl(
    private val moneyCurrencyRepo: MoneyCurrencyRepo,
) : MoneyCurrencyService {

    override fun findByName(name: String): MoneyCurrencyDto {
        return moneyCurrencyRepo.findByName(name)
    }

    override fun save(dto: MoneyCurrencyDto): MoneyCurrencyDto {
        return moneyCurrencyRepo.save(dto)
    }

    override fun saveBatch(list: List<MoneyCurrencyDto>): List<MoneyCurrencyDto> {
        val saved = list.map {
            moneyCurrencyRepo.save(it)
        }.toList()

        return saved
    }

    override fun update(dto: MoneyCurrencyDto) {
        moneyCurrencyRepo.update(dto)
    }

    override fun deleteByName(name: String) {
        moneyCurrencyRepo.deleteByName(name)
    }

    override fun findAll(): List<MoneyCurrencyDto> {
        return moneyCurrencyRepo.findAll()
    }

    override fun findByPartOfName(namePart: String): List<MoneyCurrencyDto> {
        return moneyCurrencyRepo.findByPartOfName(namePart)
    }
}