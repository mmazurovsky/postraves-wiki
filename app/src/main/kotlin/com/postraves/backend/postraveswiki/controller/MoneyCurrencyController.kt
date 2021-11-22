package com.postraves.backend.postraveswiki.controller

import com.postraves.backend.postraveswiki.data.dto.MoneyCurrencyDto
import com.postraves.backend.postraveswiki.service.MoneyCurrencyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/moneyCurrency")
class MoneyCurrencyController(
    private val moneyCurrencyService: MoneyCurrencyService,
) :
    BaseRequests<MoneyCurrencyDto, MoneyCurrencyDto>,
    ByNameRequests<MoneyCurrencyDto>,
    FindByNameRequests<MoneyCurrencyDto> {

    override fun save(dto: MoneyCurrencyDto): MoneyCurrencyDto {
        return moneyCurrencyService.save(dto)
    }

    override fun update(dto: MoneyCurrencyDto) {
        moneyCurrencyService.update(dto)
    }

    override fun findByName(name: String): MoneyCurrencyDto {
        return moneyCurrencyService.findByName(name)
    }

    @GetMapping("/all")
    override fun findAll(): List<MoneyCurrencyDto> {
        return moneyCurrencyService.findAll()
    }

    override fun deleteByName(name: String) {
        moneyCurrencyService.deleteByName(name)
    }

    override fun findByPartOfName(namePart: String): List<MoneyCurrencyDto> {
        return moneyCurrencyService.findByPartOfName(namePart)
    }

    override fun saveBatch(list: List<MoneyCurrencyDto>): List<MoneyCurrencyDto> {
        return moneyCurrencyService.saveBatch(list)
    }
}