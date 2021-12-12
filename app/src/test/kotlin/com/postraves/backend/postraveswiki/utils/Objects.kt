package com.postraves.backend.postraveswiki.utils

import com.postraves.backend.postraveswiki.data.dto.CoordinateDto
import com.postraves.backend.postraveswiki.data.dto.MoneyCurrencyDto
import com.postraves.backend.postraveswiki.data.dto.reading.CityDto
import com.postraves.backend.postraveswiki.data.dto.reading.CountryDto
import com.postraves.backend.postraveswiki.data.dto.reading.UserFullDto
import com.postraves.backend.postraveswiki.data.dto.writing.*
import com.postraves.backend.postraveswiki.data.enum.UserProfileRole
import com.postraves.backend.postraveswiki.security.SecurityFilter
import com.postraves.backend.postraveswiki.security.dataclass.Credentials
import com.postraves.backend.postraveswiki.utils.TestEntity.adminUserTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import redis.embedded.RedisExecProvider
import redis.embedded.util.Architecture
import redis.embedded.util.OS
import java.time.OffsetDateTime
import java.time.ZoneOffset

object Endpoints {
    val artistEndpoint: String = "/artist"
    val cityEndpoint: String = "/city"
    val countryEndpoint: String = "/country"
    val eventEndpoint: String = "/event"
    val unityEndpoint: String = "/unity"
    val placeEndpoint: String = "/place"
}

object MockAuthentication {
    val authAdminTest = UsernamePasswordAuthenticationToken(
        adminUserTest,
        Credentials(),
        mutableListOf(SimpleGrantedAuthority("${SecurityFilter.ROLE_PREFIX}${UserProfileRole.ADMIN.name}"))
    )

    fun createAuthByUser(user: UserFullDto?): UsernamePasswordAuthenticationToken {
        return UsernamePasswordAuthenticationToken(
            user,
            Credentials(),
            if (user != null) mutableListOf(SimpleGrantedAuthority("${SecurityFilter.ROLE_PREFIX}${user.role}")) else null
        )
    }
}

object TestEntity {
    val currencyRubTest = MoneyCurrencyDto(
        name = "RUB",
        symbol = "₽",
    )

    val currencyUsdTest = MoneyCurrencyDto(
        name = "USD",
        symbol = "$",
    )

    val currencyEurTest = MoneyCurrencyDto(
        name = "EUR",
        symbol = "€",
    )

    val countryBeTest = CountryWriteDto(
        name = "BE",
        nameRu = "NameRu",
        nameEn = "NameUk",
        nameDe = "NameDe",
        nameFr = "NameFr",
        phoneCode = "+9",
    )

    val countryRuTest = CountryWriteDto(
        name = "RU",
        nameRu = "NameRu",
        nameEn = "NameUk",
        nameDe = "NameDe",
        nameFr = "NameFr",
        phoneCode = "+7",
    )

    val countryCaTest = CountryWriteDto(
        name = "CA",
        nameRu = "NameRu",
        nameEn = "NameUk",
        nameDe = "NameDe",
        nameFr = "NameFr",
        phoneCode = "+10",
    )

    val cityBrugesTest = CityWriteDto(
        name = "Bruges",
        nameRu = "NameRu",
        nameEn = "NameUk",
        nameDe = "NameDe",
        nameFr = "NameFr",
        countryName = "BE",
        timeOffset = 0
    )

    val cityMoscowTest = CityWriteDto(
        name = "Moscow",
        nameRu = "NameRu",
        nameEn = "NameUk",
        nameDe = "NameDe",
        nameFr = "NameFr",
        countryName = "RU",
        timeOffset = 0
    )

    val cityTorontoTest = CityWriteDto(
        name = "Toronto",
        nameRu = "NameRu",
        nameEn = "NameUk",
        nameDe = "NameDe",
        nameFr = "NameFr",
        countryName = "CA",
        timeOffset = 0
    )

    val artistBeTest = ArtistWriteDto(
        id = null,
        name = "Amelie Lens",
        imageLink = "image",
        soundcloudUsername = "soundcloud",
        instagramUsername = "instagram",
        about = "About Amelie",
        countryName = countryBeTest.name,
    )

    val unityBeTest = UnityWriteDto(
        id = null,
        name = "Unity 1",
        imageLink = "image 1",
        soundcloudUsername = "soundcloud 1",
        instagramUsername = "instagram 1",
        bandcampUsername = "bandcamp 1",
        about = "About 1",
        countryName = countryBeTest.name,
    )

    val placeBrugesTest = PlaceWriteDto(
        id = null,
        name = "Club1",
        imageLink = "image1",
        soundcloudUsername = "soundcloud1",
        instagramUsername = "instagram1",
        about = "About club1",
        streetAddress = "Street address1",
        coordinate = CoordinateDto(
            latitude = 0.0,
            longitude = 0.0
        ),
        cityName = "Bruges"
    )

    val eventTest = EventWriteDto(
        id = null,
        name = "Event1",
        imageLink = "image1",
        about = "About Event1",
        ticketsLink = "link1",
        startDateTime = OffsetDateTime.of(2021, 8, 19, 0, 0, 0, 0, ZoneOffset.ofHours(0)),
        endDateTime = OffsetDateTime.of(2021, 8, 19, 6, 0, 0, 0, ZoneOffset.ofHours(0)),
        ticketPrices = emptyList(),
        // place id must be changed to real one of persisted place
        placeId = 1,
        organizers = emptySet(),
    )


    val userToSaveTest = UserWriteDto(
        name = "Mika",
        imageLink = null,
        about = null,
        instagramUsername = null,
        telegramUsername = null,
        currentCity = "Bruges"
    )

    val userTest = UserFullDto(
        id = 69,
        name = "Mika",
        currentCity = CityDto(
            name = "Bruges",
            localName = "Bruges",
            timeOffset = 1,
            country = CountryDto(
                name = "BE",
                localName = "Belgium",
                emojiCode = "",
                phoneCode = "",
            )
        ),
        about = null,
        imageLink = null,
        instagramUsername = null,
        telegramUsername = null,
        role = UserProfileRole.USER,
    )

    val adminUserTest = UserFullDto(
        id = 69,
        name = "admin",
        currentCity = CityDto(
            name = "Bruges",
            localName = "Bruges",
            timeOffset = 1,
            country = CountryDto(
                name = "BE",
                localName = "Belgium",
                emojiCode = "",
                phoneCode = "",
            )
        ),
        about = null,
        imageLink = null,
        instagramUsername = null,
        telegramUsername = null,
        role = UserProfileRole.ADMIN,
    )
}

object Components {
    val customRedisProvider: RedisExecProvider =
        RedisExecProvider.defaultProvider()
            .override(OS.MAC_OS_X, Architecture.x86_64, "/Users/mmazurovsky/Code/Redis/redis-6.2.6/src/redis-server")
            .override(OS.MAC_OS_X, Architecture.x86, "/Users/mmazurovsky/Code/Redis/redis-6.2.6/src/redis-server")
}