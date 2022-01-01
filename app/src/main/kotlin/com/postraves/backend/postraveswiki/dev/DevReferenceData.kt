package com.postraves.backend.postraveswiki.dev

import com.postraves.backend.postraveswiki.config.logger
import com.postraves.backend.postraveswiki.data.dto.CoordinateDto
import com.postraves.backend.postraveswiki.data.dto.MoneyCurrencyDto
import com.postraves.backend.postraveswiki.data.dto.reading.SceneDto
import com.postraves.backend.postraveswiki.data.dto.writing.*
import com.postraves.backend.postraveswiki.data.enum.UserProfileRole
import com.postraves.backend.postraveswiki.repo.followable.MyUserProfileRepo
import com.postraves.backend.postraveswiki.repo.followable.OtherUserRepo
import com.postraves.backend.postraveswiki.repo.quick.CleaningQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.service.CityService
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.service.MoneyCurrencyService
import com.postraves.backend.postraveswiki.service.followable.ArtistService
import com.postraves.backend.postraveswiki.service.followable.EventService
import com.postraves.backend.postraveswiki.service.followable.PlaceService
import com.postraves.backend.postraveswiki.service.followable.UnityService
import com.postraves.backend.postraveswiki.util.DateTimeProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@Profile("dev")
class DevReferenceData(
    private val currencyMoneyService: MoneyCurrencyService,
    private val countryService: CountryService,
    private val cityService: CityService,
    private val placeService: PlaceService,
    private val unityService: UnityService,
    private val artistService: ArtistService,
    private val eventService: EventService,
    private val moneyCurrencyService: MoneyCurrencyService,
    private val otherUserRepo: OtherUserRepo,
    private val myUserRepo: MyUserProfileRepo,
    private val dateTimeProvider: DateTimeProvider,
    private val quickRepoCleaner: CleaningQuickRepo,
    @Qualifier("artistWeeklyFollowersQuickRepoImpl")
    private val artistWeeklyFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("unityWeeklyFollowersQuickRepoImpl")
    private val unityWeeklyFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("placeWeeklyFollowersQuickRepoImpl")
    private val placeWeeklyFollowersQuickRepo: FollowersQuickRepo,
    @Qualifier("eventWeeklyFollowersQuickRepoImpl")
    private val eventWeeklyFollowersQuickRepo: FollowersQuickRepo,
    @Value("\${DEV_ADMIN_1_AUTH_UID}")
    private val admin1AuthUid: String,
    @Value("\${DEV_ADMIN_2_AUTH_UID}")
    private val admin2AuthUid: String,
) {

    val admin1 = UserWriteDto(
        name = "mmazurovsky",
        currentCity = "RU_Moscow"
    )

    val admin2 = UserWriteDto(
        name = "admin2",
        currentCity = "RU_Moscow"
    )

    val currencyRub = MoneyCurrencyDto(
        name = "RUB",
        symbol = "₽",
    )

    val currencyUsd = MoneyCurrencyDto(
        name = "USD",
        symbol = "$",
    )

    val currencyEur = MoneyCurrencyDto(
        name = "EUR",
        symbol = "€",
    )

    val countryRu = CountryWriteDto(
        name = "RU",
        nameRu = "Россия",
        nameEn = "Russia",
        nameDe = "Russia2",
        nameFr = "Russia3",
        phoneCode = "+7",
    )

    val countryUa = CountryWriteDto(
        name = "UA",
        nameRu = "Украина",
        nameEn = "Ukraine",
        nameDe = "Ukraine2",
        nameFr = "Ukraine3",
        phoneCode = "+380",
    )

    val countryBe = CountryWriteDto(
        name = "BE",
        nameRu = "Бельгия",
        nameEn = "Belgium",
        nameDe = "Belgium2",
        nameFr = "Belgium3",
        phoneCode = "+32",
    )

    val countryNl = CountryWriteDto(
        name = "NL",
        nameRu = "Нидерланды",
        nameEn = "Netherlands",
        nameDe = "Netherlands2",
        nameFr = "Netherlands3",
        phoneCode = "+31",
    )

    val countryDe = CountryWriteDto(
        name = "DE",
        nameRu = "Германия",
        nameEn = "Germany",
        nameDe = "Germany2",
        nameFr = "Germany3",
        phoneCode = "+49",
    )

    val countryFr = CountryWriteDto(
        name = "FR",
        nameRu = "Франция",
        nameEn = "France",
        nameDe = "France2",
        nameFr = "France3",
        phoneCode = "+33",
    )

    val countryUs = CountryWriteDto(
        name = "US",
        nameRu = "США",
        nameEn = "USA",
        nameDe = "USA2",
        nameFr = "USA3",
        phoneCode = "+1",
    )

    val countryUk = CountryWriteDto(
        name = "UK",
        nameRu = "Великобритания",
        nameEn = "UK",
        nameDe = "UK2",
        nameFr = "UK3",
        phoneCode = "+44",
    )

    val cityMoscow = CityWriteDto(
        name = "RU_Moscow",
        countryName = "RU",
        nameRu = "Москва",
        nameEn = "Moscow",
        nameDe = "Moscow2",
        nameFr = "Moscow3",
        timeOffset = 3
    )

    //todo
    val cityNewYork = CityWriteDto(
        name = "US_Newyork",
        countryName = "US",
        nameRu = "Нью Йорк",
        nameEn = "New York",
        nameDe = "New York2",
        nameFr = "New York3",
        timeOffset = -5
    )

    //todo
    val cityBerlin = CityWriteDto(
        name = "DE_Berlin",
        countryName = "DE",
        nameRu = "Берлин",
        nameEn = "Berlin",
        nameDe = "Berlin2",
        nameFr = "Berlin3",
        timeOffset = 1
    )

    //todo
    val cityAmsterdam = CityWriteDto(
        name = "NL_Amsterdam",
        countryName = "NL",
        nameRu = "Амстердам",
        nameEn = "Amsterdam",
        nameDe = "Amsterdam2",
        nameFr = "Amsterdam3",
        timeOffset = 1
    )

    val placeMutabor = PlaceWriteDto(
        id = null,
        name = "Mutabor",
        cityName = cityMoscow.name,
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_mutabor.jpeg?alt=media&token=e6a97df6-4469-4b71-bd88-c31638ce8943",
        streetAddress = "Sharikopodshipnikovskaya Ulitsa, 13, с32",
        coordinate = CoordinateDto(
            latitude = 55.719591,
            longitude = 37.686350,
        ),
        soundcloudUsername = "mutabormoscow",
        instagramUsername = "muta.bor",
        about = "Главное арт-пространство России",
    )

    val placeSlezy = PlaceWriteDto(
        id = null,
        name = "Слёзы",
        cityName = cityMoscow.name,
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_slezy.jpeg?alt=media&token=8f841c93-6c22-44c5-b820-c7abac8fd19f",
        streetAddress = "Костомаровский пер., 3",
        coordinate = CoordinateDto(
            latitude = 55.719591,
            longitude = 37.686350,
        ),
        soundcloudUsername = null,
        instagramUsername = null,
        about = null,
    )

    val placePowerhouse = PlaceWriteDto(
        id = null,
        name = "Powerhouse",
        cityName = cityMoscow.name,
        imageLink = null,
        streetAddress = "Гончарная, 7/4",
        coordinate = CoordinateDto(
            latitude = 55.719591,
            longitude = 37.686350,
        ),
        soundcloudUsername = null,
        instagramUsername = null,
        about = "about Powerhouse",
    )

    val placeGazgolder = PlaceWriteDto(
        id = null,
        name = "Gazgolder",
        cityName = cityMoscow.name,
        imageLink = null,
        streetAddress = "Нижний Сусальный пер. 5, стр. 26 ГазгольдербайБастаэнд хис паверфул френдс ГазгольдербайБастаэнд хис паверфул френдс",
        coordinate = CoordinateDto(
            latitude = 55.719591,
            longitude = 37.686350,
        ),
        soundcloudUsername = null,
        instagramUsername = null,
        about = "about Gazgolder",
    )

    //todo
    val placeBerghain = PlaceWriteDto(
        id = null,
        name = "Berghain",
        cityName = cityBerlin.name,
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Freference%2Ftest_berghain.png?alt=media&token=0f58794f-8228-4311-8efb-2558e26c55af",
        streetAddress = "Am Wriezener Bahnhof",
        coordinate = CoordinateDto(
            latitude = 52.5107083,
            longitude = 13.4345184,
        ),
        soundcloudUsername = "berghain",
        instagramUsername = "berghain_ostgut",
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
    )

    //todo
    val placeTresor = PlaceWriteDto(
        id = null,
        name = "Tresor",
        cityName = cityBerlin.name,
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Freference%2Ftest_tresor.png?alt=media&token=b9ff3b02-b723-4c18-b336-240465ac7663",
        streetAddress = "Köpenicker Str. 70",
        coordinate = CoordinateDto(
            latitude = 52.5109923,
            longitude = 13.4182732,
        ),
        soundcloudUsername = "tresorberlin",
        instagramUsername = "tresorberlin",
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
    )

    //todo
    val placeAnomalie = PlaceWriteDto(
        id = null,
        name = "Anomalie Art Club",
        cityName = cityBerlin.name,
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Freference%2Ftest_anomalie.jpeg?alt=media&token=0ed88e07-8285-4c17-9a4f-5139ad6d6c22",
        streetAddress = "Storkower Str. 123",
        coordinate = CoordinateDto(
            latitude = 52.5354221,
            longitude = 13.4530659,
        ),
        soundcloudUsername = "anomalieartclubberlin",
        instagramUsername = "anomalieartclub",
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
    )

    val placeOt301 = PlaceWriteDto(
        id = null,
        name = "OT301",
        cityName = cityAmsterdam.name,
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Freference%2Ftest_ot301.jpeg?alt=media&token=1b750ee3-2ea9-4a2d-974f-ffb826f16902",
        streetAddress = "Overtoom 301",
        coordinate = CoordinateDto(
            latitude = 52.3600834,
            longitude = 4.8656877,
        ),
        soundcloudUsername = null,
        instagramUsername = "ot301adam",
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
    )

    val placeRadion = PlaceWriteDto(
        id = null,
        name = "Radion",
        cityName = cityAmsterdam.name,
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Freference%2Ftest_radion.jpeg?alt=media&token=bee86fb3-9444-49af-aef9-9c722dcbdb4e",
        streetAddress = "Louwesweg 1",
        coordinate = CoordinateDto(
            latitude = 52.3455842,
            longitude = 4.82565,
        ),
        soundcloudUsername = null,
        instagramUsername = "radionamsterdam",
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
    )

    val sceneBerghainMain = SceneDto(
        id = null,
        name = "Main",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_mutabor_main.jpeg?alt=media&token=8a7e4df6-5742-40c5-85b3-566793ff79ce",
        priority = 3,
    )

    val sceneBerghainMedium = SceneDto(
        id = null,
        name = "Medium",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_mutabor_medium.jpeg?alt=media&token=80a232a0-0864-4d56-b3c4-9b3327af89af",
        priority = 2,
    )

    val sceneBerghainGarden = SceneDto(
        id = null,
        name = "Garden",
        imageLink = null,
        priority = 1,
    )

    val unityLenske = UnityWriteDto(
        id = null,
        name = "Lenske Records",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_lenske.jpeg?alt=media&token=80d2bf88-38a6-4983-a098-4de15581a0a9",
        countryName = "FR",
        soundcloudUsername = "lenskerecords",
        instagramUsername = null,
        bandcampUsername = null,
        about = "About Lenske",
    )

    val unitySystem = UnityWriteDto(
        id = null,
        name = "System 108",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_system.jpeg?alt=media&token=075cb18f-2d8a-4e66-b807-f1561365419d",
        countryName = "RU",
        soundcloudUsername = "system108",
        instagramUsername = "system108",
        bandcampUsername = "system108",
        about = "about System",
    )

    val unityArma = UnityWriteDto(
        id = null,
        name = "ARMA",
        imageLink = null,
        countryName = "RU",
        soundcloudUsername = "arma17",
        instagramUsername = "arma17ru",
        bandcampUsername = "armarecords",
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
    )

    val unityMonasterio = UnityWriteDto(
        id = null,
        name = "Monasterio",
        imageLink = null,
        countryName = "RU",
        soundcloudUsername = null,
        instagramUsername = null,
        bandcampUsername = null,
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
    )

    val unityRabitsa = UnityWriteDto(
        id = null,
        name = "Рабица",
        imageLink = null,
        countryName = "RU",
        soundcloudUsername = null,
        instagramUsername = null,
        bandcampUsername = null,
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
    )

    val unityNii = UnityWriteDto(
        id = null,
        name = "НИИ",
        imageLink = null,
        countryName = "RU",
        soundcloudUsername = null,
        instagramUsername = null,
        bandcampUsername = null,
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
    )

    val artistAbelle = ArtistWriteDto(
        id = null,
        name = "Abelle",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_abelle.jpeg?alt=media&token=308b41e3-025c-4af0-ac5f-891f673c6554",
        countryName = "RU",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistMash = ArtistWriteDto(
        id = null,
        name = "Mash",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_mashkov.png?alt=media&token=545f3b5b-c126-43c4-99d3-ca6735baea50",
        countryName = "DE",
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
        instagramUsername = "mashkov",
        soundcloudUsername = "mashkov",
    )

    val artistMujuice = ArtistWriteDto(
        id = null,
        name = "Mujuice",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_mujuice.jpeg?alt=media&token=1247bd99-20aa-420d-ad81-907ec60d2529",
        countryName = "RU",
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
        instagramUsername = "mujuice",
        soundcloudUsername = "mujuice",
    )

    val artistRodina = ArtistWriteDto(
        id = null,
        name = "Sofia",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_rodina.jpeg?alt=media&token=b802b2a4-de1d-417b-bdf2-d5f0971b232a",
        countryName = "DE",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistAllien = ArtistWriteDto(
        id = null,
        name = "Ellen Allien",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_allien.jpeg?alt=media&token=2af8d768-cceb-4f68-b340-0d79a60350e0",
        countryName = "DE",
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
        instagramUsername = "ellen.allien",
        soundcloudUsername = "ellen-allien",
    )

    val artistCuve = ArtistWriteDto(
        id = null,
        name = "Clara Cuvé",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_cuve.jpeg?alt=media&token=24ac822b-6a07-49a7-b25f-d760aacfefae",
        countryName = "FR",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistModels = ArtistWriteDto(
        id = null,
        name = "I HATE MODELS",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_models.jpeg?alt=media&token=d1da6eeb-fe9d-4f2c-af53-475d81d30723",
        countryName = "FR",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistParfait = ArtistWriteDto(
        id = null,
        name = "Parfait",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_parfait.jpeg?alt=media&token=11b1e2fb-4255-4a31-a567-4179d4e6e920",
        countryName = "FR",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistLens = ArtistWriteDto(
        id = null,
        name = "Amelie Lens",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_lens.jpeg?alt=media&token=dd011453-dc28-4bd6-9fed-88dd0438d20a",
        countryName = "BE",
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
        instagramUsername = "amelie_lens",
        soundcloudUsername = "AMELIELENS",
    )

    val artistFarrago = ArtistWriteDto(
        id = null,
        name = "Farrago",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_farrago.jpeg?alt=media&token=e073cfb9-a322-4d94-9716-20bde719246b",
        countryName = "BE",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistBejenec = ArtistWriteDto(
        id = null,
        name = "BEJENEC",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_bejenec.jpeg?alt=media&token=4a14bddf-3084-4460-8d9d-1a08f8d60574",
        countryName = "UA",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistKolosova = ArtistWriteDto(
        id = null,
        name = "Daria Kolosova",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_kolosova.jpeg?alt=media&token=e3040889-e79b-42d8-ade8-3c4b0ce2e062",
        countryName = "UA",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistVillalobos = ArtistWriteDto(
        id = null,
        name = "Ricardo Villalobos",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_villalobos.jpeg?alt=media&token=f0078e7b-cfd4-46ef-b5bf-31957d53f8b6",
        countryName = "DE",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistGorbachev = ArtistWriteDto(
        id = null,
        name = "Philipp",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_gorbachev.jpeg?alt=media&token=f67f1f71-2908-4541-ae75-703335a4bd02",
        countryName = "NL",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistZots = ArtistWriteDto(
        id = null,
        name = "Zots",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_zots.jpg?alt=media&token=92295ce3-e963-4af7-bcfa-f9f997a5b87d",
        countryName = "DE",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistChronic = ArtistWriteDto(
        id = null,
        name = "Chronic",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Ftest_chronic.jpeg?alt=media&token=5a3a7f48-52c5-4ea6-af72-04da0c9b2ed9",
        countryName = "DE",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistWitte = ArtistWriteDto(
        id = null,
        name = "Charlotte De Witte",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Freference%2Ftest_witte.jpeg?alt=media&token=33501902-8229-449f-90a5-2d4e7bb2e466",
        countryName = "BE",
        about = null,
        instagramUsername = "charlottedewittemusic",
        soundcloudUsername = "charlottedewittemusic",
    )

    val artistCox = ArtistWriteDto(
        id = null,
        name = "Carl Cox",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Freference%2Ftest_cox.jpeg?alt=media&token=2582bfda-4c7e-408b-9c60-d6d51112a327",
        countryName = "UK",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistZabelin = ArtistWriteDto(
        id = null,
        name = "NZ666",
        imageLink = "https://firebasestorage.googleapis.com/v0/b/postraves.appspot.com/o/test%2Fimages%2Freference%2Ftest_zabelin.jpeg?alt=media&token=ae08ba32-880b-4c30-bb2b-e8d3f8def041",
        countryName = "NL",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    @EventListener(ApplicationReadyEvent::class)
    fun manageData() {
        quickRepoCleaner.clearAllData()
        countryService.findAll().forEach { countryService.deleteByName(it.name) }
        otherUserRepo.findAll().forEach { otherUserRepo.deleteById(it.id) }
        cityService.findAll().forEach { cityService.deleteByName(it.name) }
        placeService.findAll().forEach { placeService.deleteById(it.id) }
        unityService.findAll().forEach { unityService.deleteById(it.id) }
        artistService.findAll().forEach { artistService.deleteById(it.id) }
        eventService.findAll().forEach { eventService.deleteById(it.id) }
        moneyCurrencyService.findAll().forEach { moneyCurrencyService.deleteByName(it.name) }

        writeReferenceData()
    }

    fun writeReferenceData() {
        val savedCountries = countryService.saveBatch(
            listOf(
                countryRu,
                countryUa,
                countryBe,
                countryDe,
                countryFr,
                countryUs,
                countryNl,
                countryUk,
            )
        )

        val savedCurrencies = currencyMoneyService.saveBatch(
            listOf(
                currencyRub,
                currencyUsd,
                currencyEur,
            )
        )

        val savedCities = cityService.saveBatch(
            listOf(
                cityMoscow,
                cityNewYork,
                cityBerlin,
                cityAmsterdam,
            )
        )

        val savedAdminM = myUserRepo.saveWithSpecialRole(UserProfileRole.ADMIN, admin1, admin1AuthUid)
        val savedAdminVibe = myUserRepo.saveWithSpecialRole(UserProfileRole.ADMIN, admin2, admin2AuthUid)

        val placeMutaborSaved = placeService.save(placeMutabor)
        val placeGazgoldeSaved = placeService.save(placeGazgolder)
        val placePowerhouseSaved = placeService.save(placePowerhouse)
        val placeSlezySaved = placeService.save(placeSlezy)
        val placeBerghainSaved = placeService.save(placeBerghain)
        val placeTresorSaved = placeService.save(placeTresor)
        val placeAnomalieSaved = placeService.save(placeAnomalie)
        val placeOt301Saved = placeService.save(placeOt301)
        val placeRadionSaved = placeService.save(placeRadion)

        placeService.updateScenesOfPlace(
            placeBerghainSaved.id,
            listOf(
                sceneBerghainMain,
                sceneBerghainMedium,
                sceneBerghainGarden
            )
        )

        val scenesBerghainSaved = placeService.getAllScenes()

        val unityLenskeSaved = unityService.save(unityLenske)
        val unitySystemSaved = unityService.save(unitySystem)
        val unityArmaSaved = unityService.save(unityArma)
        val unityMonasterioSaved = unityService.save(unityMonasterio)
        val unityRabitsaSaved = unityService.save(unityRabitsa)
        val unityNiiSaved = unityService.save(unityNii)

        val artistAbelleSaved = artistService.save(artistAbelle)
        val artistAllienSaved = artistService.save(artistAllien)
        val artistBejenecSaved = artistService.save(artistBejenec)
        val artistCuveSaved = artistService.save(artistCuve)
        val artistChronicSaved = artistService.save(artistChronic)
        val artistFarragoSaved = artistService.save(artistFarrago)
        val artistGorbachevSaved = artistService.save(artistGorbachev)
        val artistKolosovaSaved = artistService.save(artistKolosova)
        val artistLensSaved = artistService.save(artistLens)
        val artistMashkovSaved = artistService.save(artistMash)
        val artistModelsSaved = artistService.save(artistModels)
        val artistMujuiceSaved = artistService.save(artistMujuice)
        val artistParfaitSaved = artistService.save(artistParfait)
        val artistRodinaSaved = artistService.save(artistRodina)
        val artistVillalobosSaved = artistService.save(artistVillalobos)
        val artistZotsSaved = artistService.save(artistZots)
        val artistWitteSaved = artistService.save(artistWitte)
        val artistCoxSaved = artistService.save(artistCox)
        val artistZabelinSaved = artistService.save(artistZabelin)

        unityService.updateArtistsOfUnity(
            unityLenskeSaved.id, setOf(
                artistLensSaved.id,
                artistFarragoSaved.id,
            )
        )

        unityService.updateArtistsOfUnity(
            unitySystemSaved.id, setOf(
                artistMashkovSaved.id,
                artistGorbachevSaved.id,
                artistKolosovaSaved.id,
                artistBejenecSaved.id,
                artistChronicSaved.id,
            )
        )

        unityService.updateArtistsOfUnity(
            unityArmaSaved.id, setOf(
                artistAbelleSaved.id,
                artistZotsSaved.id,
            )
        )

        val eventRadost = EventWriteDto(
            id = null,
            name = "Autumn pleasure",
            imageLink = "https://sun9-3.userapi.com/impf/6fhybramftz3iKZyjnMYWEkuOWwGGbRlNyWjAA/YbeX05dtrnI.jpg?size=807x422&quality=96&sign=334eeab97e92f0957467dc2d6ae0bee3&type=album",
            about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
            ticketsLink = null,
            startDateTime = dateTimeProvider.getNow().minusDays(1),
            endDateTime = dateTimeProvider.getNow().minusDays(1).plusHours(5),
            ticketPrices = listOf(
                TicketPriceWriteDto(
                    name = "One",
                    price = 20.0,
                    currency = currencyEur.name
                )
            ),
            placeId = placeBerghainSaved.id,
            organizers = setOf(
                unityArmaSaved.id
            )
        )

        val eventCombo = EventWriteDto(
            id = null,
            name = "Combo",
            imageLink = "https://sun9-44.userapi.com/impf/DKGPcayeA1sy1mQZS1HSudF0qBAkNLRiPuMGAA/nnkC0XLby-0.jpg?size=807x436&quality=96&sign=259845352f55d8d9cb02caebb8126d0f&type=album",
            about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
            ticketsLink = null,
            startDateTime = dateTimeProvider.getNow().minusDays(1).plusHours(5),
            endDateTime = dateTimeProvider.getNow().minusHours(5),
            ticketPrices = listOf(
                TicketPriceWriteDto(
                    name = "После 20:00", // todo sort prices by value on GET
                    price = 100.0,
                    currency = currencyEur.name
                ),
                TicketPriceWriteDto(
                    name = "До 20:00",
                    price = 0.0,
                    currency = currencyEur.name
                ),
            ),
            placeId = placeAnomalieSaved.id,
            organizers = setOf(
                unityMonasterioSaved.id,
                unityNiiSaved.id,
                unityRabitsaSaved.id,
                unityArmaSaved.id,
            )
        )

        val eventHyperboloid = EventWriteDto(
            id = null,
            name = "Hyperboloid Night One Two Three Four Five Six Seven Eight Nine Ten",
            imageLink = null,
            about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
            ticketsLink = null,
            startDateTime = dateTimeProvider.getNow().plusDays(12),
            endDateTime = dateTimeProvider.getNow().plusDays(12).plusHours(8),
            ticketPrices = null,
            placeId = placeTresorSaved.id,
            organizers = setOf(
            )
        )

        val eventSanchez = EventWriteDto(
            id = null,
            name = "Sanchez Fiesta",
            imageLink = "https://sun9-70.userapi.com/impf/HcGo_gSFS9emEUabJI130FcFGgDWxS5Sv-N5wQ/FojhJi2t3_4.jpg?size=807x424&quality=96&sign=4cc06a1cbe7a106e6a0b1bcdb7fb2da1&type=album",
            about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
            ticketsLink = "google.com",
            startDateTime = dateTimeProvider.getNow().plusDays(8),
            endDateTime = dateTimeProvider.getNow().plusDays(8).plusHours(8),
            ticketPrices = listOf(
                TicketPriceWriteDto(
                    name = "Free",
                    price = 0.0,
                    currency = currencyEur.name
                ),
                TicketPriceWriteDto(
                    name = "После 00:00",
                    price = 1000.0,
                    currency = currencyEur.name
                ),
                TicketPriceWriteDto(
                    name = "После 6:00",
                    price = 500.0,
                    currency = currencyEur.name
                ),
            ),
            placeId = placeTresorSaved.id,
            organizers = setOf(
            )
        )

        val eventApplique = EventWriteDto(
            id = null,
            name = "Applique: Golden Hits",
            imageLink = "https://sun9-6.userapi.com/impf/GTw48P6DtpHtoHwsfj4jIuLtrhvuqH7FskfsJw/QBjtlCNata0.jpg?size=807x423&quality=96&sign=b7f6613d28ffed9c8e80682f47d77f6b&type=album",
            about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
            ticketsLink = "google.com",
            startDateTime = dateTimeProvider.getNow().plusDays(4),
            endDateTime = dateTimeProvider.getNow().plusDays(5).plusHours(8),
            ticketPrices = listOf(
                TicketPriceWriteDto(
                    name = "До 00:00",
                    price = 10000.0,
                    currency = currencyEur.name
                ),
                TicketPriceWriteDto(
                    name = "После 00:00",
                    price = 12000.0,
                    currency = currencyEur.name
                ),
            ),
            placeId = placeAnomalieSaved.id,
            organizers = setOf(
            )
        )

        val eventSynchron = EventWriteDto(
            id = null,
            name = "Synchron",
            imageLink = "https://sun9-63.userapi.com/impf/1YN0U_HwapP6kQviE95Jg85obf41TdXsOFgPqQ/CKAql91bu94.jpg?size=807x422&quality=96&sign=ce58f9016e4f5a3fda579ad1834c995c&type=album",
            about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
            ticketsLink = "google.com",
            startDateTime = dateTimeProvider.getNow().plusDays(7),
            endDateTime = dateTimeProvider.getNow().plusDays(7).plusHours(5),
            ticketPrices = listOf(
                TicketPriceWriteDto(
                    name = "До 00:00",
                    price = 50.0,
                    currency = currencyEur.name
                ),
                TicketPriceWriteDto(
                    name = "После 00:00",
                    price = 80.0,
                    currency = currencyEur.name
                ),
            ),
            placeId = placeBerghainSaved.id,
            organizers = setOf(
            )
        )

        val eventRabitsa = EventWriteDto(
            id = null,
            name = "Рабица х НИИ",
            imageLink = "https://sun9-17.userapi.com/impf/JfHbAwbSVcc_dvkeW9fQHfGrzlTfWmlHcIAslw/XNs0T_F2AHw.jpg?size=807x367&quality=96&sign=e305c479e13c39574d10472b2af61ad0&type=album",
            about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
            ticketsLink = "google.com",
            startDateTime = dateTimeProvider.getNow().plusDays(8),
            endDateTime = dateTimeProvider.getNow().plusDays(8).plusHours(5),
            ticketPrices = listOf(
                TicketPriceWriteDto(
                    name = "До 00:00",
                    price = 10.0,
                    currency = currencyEur.name
                ),
                TicketPriceWriteDto(
                    name = "После 00:00",
                    price = 23.0,
                    currency = currencyEur.name
                ),
                TicketPriceWriteDto(
                    name = "После 08:00",
                    price = 0.0,
                    currency = currencyEur.name
                ),
            ),
            placeId = placeMutaborSaved.id,
            organizers = setOf(
                unityNiiSaved.id,
                unityRabitsaSaved.id,
            )
        )

        val eventVillalobos = EventWriteDto(
            id = null,
            name = "Ricardo Villalobos",
            imageLink = null,
            about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
            ticketsLink = "google.com",
            startDateTime = dateTimeProvider.getNow().plusDays(8).plusHours(1),
            endDateTime = dateTimeProvider.getNow().plusDays(8).plusHours(9),
            ticketPrices = listOf(
                TicketPriceWriteDto(
                    name = "До 00:00",
                    price = 500.0,
                    currency = currencyEur.name
                ),
                TicketPriceWriteDto(
                    name = "После 00:00",
                    price = 1800.0,
                    currency = currencyEur.name
                ),
                TicketPriceWriteDto(
                    name = "После 08:00",
                    price = 0.0,
                    currency = currencyEur.name
                ),
            ),
            placeId = placeBerghainSaved.id,
            organizers = setOf(
            )
        )

        val eventMonasterio = EventWriteDto(
            id = null,
            name = "Monasterio Factory 2021",
            imageLink = "https://cdn.stayhappening.com/events5/banners/e01d1aecbc7639d37e3039cc073e6dc827de50c8f688a281e1faf9fbfec57603-rimg-w526-h296-gmir?v=1622124795",
            about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
            ticketsLink = "google.com",
            startDateTime = dateTimeProvider.getNow().plusDays(10).plusHours(1),
            endDateTime = dateTimeProvider.getNow().plusDays(10).plusHours(9),
            ticketPrices = listOf(
                TicketPriceWriteDto(
                    name = "До 00:00",
                    price = 500.0,
                    currency = currencyEur.name
                ),
                TicketPriceWriteDto(
                    name = "После 00:00",
                    price = 1800.0,
                    currency = currencyEur.name
                ),
                TicketPriceWriteDto(
                    name = "После 08:00",
                    price = 0.0,
                    currency = currencyEur.name
                ),
            ),
            placeId = placeOt301Saved.id,
            organizers = setOf(
                unityMonasterioSaved.id
            )
        )

        val eventPax = EventWriteDto(
            id = null,
            name = "PAX II w/ Amelie Lens",
            imageLink = "https://i.ibb.co/VVwZrK5/195177326-4190550444338857-1452351166020055900-n.jpg",
            about = null,
            ticketsLink = "https://system108.com/pax",
            startDateTime = dateTimeProvider.getNow().minusHours(2),
            endDateTime = dateTimeProvider.getNow().plusDays(3).plusHours(10),
            ticketPrices = listOf(
                TicketPriceWriteDto(
                    name = "До 00:00",
                    price = 500.0,
                    currency = currencyEur.name
                ),
                TicketPriceWriteDto(
                    name = "После 00:00",
                    price = 1800.0,
                    currency = currencyEur.name
                ),
                TicketPriceWriteDto(
                    name = "После 08:00",
                    price = 0.0,
                    currency = currencyEur.name
                ),
            ),
            placeId = placeRadionSaved.id,
            organizers = setOf(
                unitySystemSaved.id,
                unityLenskeSaved.id,
            )
        )

        val eventSystem = EventWriteDto(
            id = null,
            name = "System One Hundred Eight",
            imageLink = "https://sun9-32.userapi.com/impf/6iUkR2dK_tKA_XrtK_uDC1S84Wi6A798V9F1sQ/5_vyoPxQnKI.jpg?size=807x422&quality=96&sign=d9392037eda29e1270fbce125735c89d&type=album",
            about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
            ticketsLink = "google.com",
            startDateTime = dateTimeProvider.getNow().plusHours(1),
            endDateTime = dateTimeProvider.getNow().plusDays(3).plusHours(12),
            ticketPrices = listOf(
                TicketPriceWriteDto(
                    name = "One",
                    price = 0.0,
                    currency = currencyRub.name
                )
            ),
            placeId = placeBerghainSaved.id,
            organizers = setOf(
                unitySystemSaved.id,
                unityArmaSaved.id,
            )
        )

        val eventSystemSaved = eventService.save(eventSystem)
        val eventRadostSaved = eventService.save(eventRadost)
        val eventSanchezSaved = eventService.save(eventSanchez)
        val eventSynchronSaved = eventService.save(eventSynchron)
        val eventComboSaved = eventService.save(eventCombo)
        val eventAppliqueSaved = eventService.save(eventApplique)
        val eventHyperboloidSaved = eventService.save(eventHyperboloid)
        val eventRabitsaSaved = eventService.save(eventRabitsa)
        val eventPaxSaved = eventService.save(eventPax)
        val eventVillalobosSaved = eventService.save(eventVillalobos)
        val eventMonasterioSaved = eventService.save(eventMonasterio)

        val performance1 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = null,
            artistIds = setOf(artistZotsSaved.id),
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null
        )

        val performance3 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesBerghainSaved[0].id,
            artistIds = setOf(artistAllienSaved.id, artistCuveSaved.id),
            typeOfPerformance = null,
            startingDateTime = dateTimeProvider.getNow().minusHours(2),
            endingDateTime = dateTimeProvider.getNow().plusMinutes(1),
        )

        val performance4 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesBerghainSaved[0].id,
            artistIds = setOf(artistMashkovSaved.id),
            typeOfPerformance = null,
            startingDateTime = dateTimeProvider.getNow().minusHours(4),
            endingDateTime = dateTimeProvider.getNow().minusHours(2)
        )

        val performance5 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesBerghainSaved[0].id,
            artistIds = setOf(artistBejenecSaved.id),
            typeOfPerformance = null,
            startingDateTime = dateTimeProvider.getNow().plusMinutes(2),
            endingDateTime = dateTimeProvider.getNow().plusHours(1)
        )

        val performance6 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesBerghainSaved[1].id,
            artistIds = setOf(artistGorbachevSaved.id),
            typeOfPerformance = null,
            startingDateTime = dateTimeProvider.getNow().minusHours(9),
            endingDateTime = dateTimeProvider.getNow().minusHours(1)
        )

        val performance7 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesBerghainSaved[1].id,
            artistIds = setOf(artistMashkovSaved.id),
            typeOfPerformance = "Trance music",
            startingDateTime = dateTimeProvider.getNow().plusHours(1),
            endingDateTime = dateTimeProvider.getNow().plusHours(6)
        )

        val performance8 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesBerghainSaved[1].id,
            artistIds = setOf(artistLensSaved.id),
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null
        )

        val performance9 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesBerghainSaved[1].id,
            artistIds = setOf(artistFarragoSaved.id),
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null
        )

        eventService.updateTimetableForEvent(
            eventSystemSaved.id, setOf(
                performance1,
                performance3,
                performance4,
                performance5,
                performance6,
                performance7,
            )
        )

        eventService.updateTimetableForEvent(
            eventPaxSaved.id, setOf(
                performance8,
                performance9,
            )
        )

        var i = 0
        while (i < 50) {
            eventService.incrementFollowersUnsafe(eventPaxSaved.id)
            i++
        }

        i = 0
        while (i < 60) {
            eventService.incrementFollowersUnsafe(eventSystemSaved.id)
            i++
        }

        i = 0
        while (i < 30) {
            unityService.incrementFollowersUnsafe(unityLenskeSaved.id)
            i++
        }

        i = 0
        while (i < 40) {
            unityService.incrementFollowersUnsafe(unitySystemSaved.id)
            i++
        }

        i = 0
        while (i < 300) {
            placeService.incrementFollowersUnsafe(placeMutaborSaved.id)
            i++
        }

        i = 0
        while (i < 10) {
            artistService.incrementFollowersUnsafe(artistAbelleSaved.id)
            i++
        }

        i = 0
        while (i < 55) {
            artistService.incrementFollowersUnsafe(artistMujuiceSaved.id)
            i++
        }

        i = 0
        while (i < 25) {
            artistService.incrementFollowersUnsafe(artistMashkovSaved.id)
            i++
        }

        i = 0
        while (i < 35) {
            artistService.incrementFollowersUnsafe(artistBejenecSaved.id)
            i++
        }

        i = 0
        while (i < 84) {
            artistService.incrementFollowersUnsafe(artistCuveSaved.id)
            i++
        }

        i = 0
        while (i < 109) {
            artistService.incrementFollowersUnsafe(artistAllienSaved.id)
            i++
        }

        i = 0
        while (i < 75) {
            artistService.incrementFollowersUnsafe(artistLensSaved.id)
            i++
        }

        i = 0
        while (i < 20) {
            artistService.incrementFollowersUnsafe(artistFarragoSaved.id)
            i++
        }

        i = 0
        while (i < 32) {
            artistService.incrementFollowersUnsafe(artistZabelinSaved.id)
            i++
        }

        i = 0
        while (i < 99) {
            artistService.incrementFollowersUnsafe(artistCoxSaved.id)
            i++
        }

        i = 0
        while (i < 123) {
            artistService.incrementFollowersUnsafe(artistWitteSaved.id)
            i++
        }

        artistService.setBestOfTheWeekForAllCities()

        artistWeeklyFollowersQuickRepo.returnAllValuesToInitial()
        unityWeeklyFollowersQuickRepo.returnAllValuesToInitial()
        placeWeeklyFollowersQuickRepo.returnAllValuesToInitial()
        eventWeeklyFollowersQuickRepo.returnAllValuesToInitial()

        i = 0
        while (i < 263) {
            eventService.incrementFollowersUnsafe(eventPaxSaved.id)
            i++
        }

        i = 0
        while (i < 174) {
            eventService.incrementFollowersUnsafe(eventSystemSaved.id)
            i++
        }

        i = 0
        while (i < 24) {
            unityService.incrementFollowersUnsafe(unityLenskeSaved.id)
            i++
        }

        i = 0
        while (i < 39) {
            unityService.incrementFollowersUnsafe(unitySystemSaved.id)
            i++
        }

        i = 0
        while (i < 31) {
            placeService.incrementFollowersUnsafe(placeMutaborSaved.id)
            i++
        }

        i = 0
        while (i < 8) {
            artistService.incrementFollowersUnsafe(artistAbelleSaved.id)
            i++
        }

        i = 0
        while (i < 2) {
            artistService.decrementFollowersUnsafe(artistMujuiceSaved.id)
            i++
        }

        i = 0
        while (i < 5) {
            artistService.decrementFollowersUnsafe(artistMashkovSaved.id)
            i++
        }

        i = 0
        while (i < 34) {
            artistService.incrementFollowersUnsafe(artistLensSaved.id)
            i++
        }

        i = 0
        while (i < 29) {
            artistService.incrementFollowersUnsafe(artistFarragoSaved.id)
            i++
        }

        logger.info("Reference data is written")
    }
}