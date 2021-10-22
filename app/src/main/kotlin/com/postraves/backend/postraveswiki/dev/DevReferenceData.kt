package com.postraves.backend.postraveswiki.dev

import com.postraves.backend.postraveswiki.config.logger
import com.postraves.backend.postraveswiki.data.dto.CoordinateDto
import com.postraves.backend.postraveswiki.data.dto.writing.CountryWriteDto
import com.postraves.backend.postraveswiki.data.dto.TicketPriceDto
import com.postraves.backend.postraveswiki.data.dto.reading.SceneDto
import com.postraves.backend.postraveswiki.data.dto.writing.*
import com.postraves.backend.postraveswiki.data.enum.MoneyCurrency
import com.postraves.backend.postraveswiki.repo.followable.OtherUserRepo
import com.postraves.backend.postraveswiki.repo.quick.CleaningQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.FollowersQuickRepo
import com.postraves.backend.postraveswiki.service.CityService
import com.postraves.backend.postraveswiki.service.CountryService
import com.postraves.backend.postraveswiki.service.followable.ArtistService
import com.postraves.backend.postraveswiki.service.followable.EventService
import com.postraves.backend.postraveswiki.service.followable.PlaceService
import com.postraves.backend.postraveswiki.service.followable.UnityService
import com.postraves.backend.postraveswiki.util.DateTimeProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@Profile("dev")
class DevReferenceData(
    private val countryService: CountryService,
    private val cityService: CityService,
    private val placeService: PlaceService,
    private val unityService: UnityService,
    private val artistService: ArtistService,
    private val eventService: EventService,
    private val otherUserRepo: OtherUserRepo,
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
) {

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

    val cityMoscow = CityWriteDto(
        name = "RU_Moscow",
        countryName = "RU",
        nameRu = "Москва",
        nameEn = "Moscow",
        nameDe = "Moscow2",
        nameFr = "Moscow3",
        timeOffset = 3
    )

    val cityPetersburg = CityWriteDto(
        name = "RU_Petersburg",
        countryName = "RU",
        nameRu = "Санкт-Петербург",
        nameEn = "Saint-Petersburg",
        nameDe = "Saint-Petersburg2",
        nameFr = "Saint-Petersburg3",
        timeOffset = 3
    )

    val placeMutabor = PlaceWriteDto(
        id = null,
        name = "Mutabor",
        cityName = cityMoscow.name,
        imageLink = "https://mutabor.club/img/mutabor.jpg",
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
        imageLink = "https://gotoparty.ru/public/img/upload/page/4819/a4cf318f0783514426f29cb0d2e0650c_200x200.jpg",
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

    val sceneMutaborMain = SceneDto(
        id = null,
        name = "Main",
        imageLink = "https://www.restoclub.ru/uploads/place_thumbnail_big/7/a/4/c/7a4ccc935aa721cc69fce4d716c5a544.jpg",
        priority = 3,
    )

    val sceneMutaborMedium = SceneDto(
        id = null,
        name = "Medium",
        imageLink = "https://ra.co/images/features/2020/mutabor-scenes05.jpg",
        priority = 2,
    )

    val sceneMutaborGarden = SceneDto(
        id = null,
        name = "Garden",
        imageLink = null,
        priority = 1,
    )

    val unityLenske = UnityWriteDto(
        id = null,
        name = "Lenske Records",
        imageLink = "https://i1.sndcdn.com/avatars-000349628618-0tza1o-t500x500.jpg",
        countryName = "FR",
        soundcloudUsername = "lenskerecords",
        instagramUsername = null,
        bandcampUsername = null,
        about = "About Lenske",
    )

    val unitySystem = UnityWriteDto(
        id = null,
        name = "System 108",
        imageLink = "https://www.residentadvisor.net/images/labels/system108.jpg",
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
        imageLink = "https://i1.sndcdn.com/avatars-000006616275-8t6obr-t500x500.jpg",
        countryName = "RU",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistMashkov = ArtistWriteDto(
        id = null,
        name = "Mashkov",
        imageLink = "https://photos.bandsintown.com/thumb/8065761.jpeg",
        countryName = "RU",
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
        instagramUsername = "mashkov",
        soundcloudUsername = "mashkov",
    )

    val artistMujuice = ArtistWriteDto(
        id = null,
        name = "Mujuice",
        imageLink = "https://i1.sndcdn.com/avatars-zubprYWZWJCMUPe7-Nlui4Q-t500x500.jpg",
        countryName = "RU",
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
        instagramUsername = "mujuice",
        soundcloudUsername = "mujuice",
    )

    val artistRodina = ArtistWriteDto(
        id = null,
        name = "Sofia Rodina",
        imageLink = "https://i1.sndcdn.com/avatars-000114541978-sixgd1-t500x500.jpg",
        countryName = "RU",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistAllien = ArtistWriteDto(
        id = null,
        name = "Ellen Allien",
        imageLink = "https://geo-static.traxsource.com/files/artists/5349.jpg",
        countryName = "DE",
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
        instagramUsername = "ellen.allien",
        soundcloudUsername = "ellen-allien",
    )

    val artistCuve = ArtistWriteDto(
        id = null,
        name = "Clara Cuvé",
        imageLink = "https://geo-media.beatport.com/image_size/500x500/10a6d60c-098d-4497-b710-85fee4ec1d9c.jpg",
        countryName = "FR",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistModels = ArtistWriteDto(
        id = null,
        name = "I HATE MODELS",
        imageLink = "https://ravemovement.com/wp-content/uploads/2020/01/i-hate-models-rave-movement.jpg",
        countryName = "FR",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistParfait = ArtistWriteDto(
        id = null,
        name = "Parfait",
        imageLink = "https://i1.sndcdn.com/artworks-000105255737-c3bl9u-t500x500.jpg",
        countryName = "FR",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistLens = ArtistWriteDto(
        id = null,
        name = "Amelie Lens",
        imageLink = "https://www.amsterdam-dance-event.nl/uploads/images/artists-speakers/_AUTOxAUTO_crop_center-center_none/13717210_1809902755895840_9030645021916190431_o_77261.jpg",
        countryName = "BE",
        about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
        instagramUsername = "amelie_lens",
        soundcloudUsername = "AMELIELENS",
    )

    val artistFarrago = ArtistWriteDto(
        id = null,
        name = "Farrago",
        imageLink = "https://i1.sndcdn.com/avatars-000650903115-zk4q63-t500x500.jpg",
        countryName = "BE",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistBejenec = ArtistWriteDto(
        id = null,
        name = "BEJENEC",
        imageLink = "https://f4.bcbits.com/img/a3619367416_2.jpg",
        countryName = "UA",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistKolosova = ArtistWriteDto(
        id = null,
        name = "Daria Kolosova",
        imageLink = "https://i1.sndcdn.com/avatars-kvYrVE26X0kvpKQ2-qty6jA-t500x500.jpg",
        countryName = "UA",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistVillalobos = ArtistWriteDto(
        id = null,
        name = "Ricardo Villalobos",
        imageLink = "https://mixmag.asia/assets/uploads/images/_full/Ricardo-Villalobos-sq.jpg",
        countryName = "DE",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistGorbachev = ArtistWriteDto(
        id = null,
        name = "Philipp Gorbachev",
        imageLink = "http://orchid-am.com/wp-content/uploads/2020/02/Philipp-Gorbachev-2019-%C2%A9-Camille-Blake-1-900x900.jpg",
        countryName = "RU",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistZots = ArtistWriteDto(
        id = null,
        name = "Andrey Zots",
        imageLink = "https://img.discogs.com/tsUcfpy11hvkPWZqoAbAZi2mFoU=/fit-in/300x300/filters:strip_icc():format(jpeg):mode_rgb():quality(40)/discogs-images/A-995736-1597376234-9390.jpeg.jpg",
        countryName = "RU",
        about = null,
        instagramUsername = null,
        soundcloudUsername = null,
    )

    val artistChronic = ArtistWriteDto(
        id = null,
        name = "Chronic Preview",
        imageLink = "https://i1.sndcdn.com/avatars-fzyLziCt2hJ6BqZs-AKsy9g-t500x500.jpg",
        countryName = "RU",
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

        writeReferenceData()
    }

    fun writeReferenceData() {
        val savedCountries = countryService.saveBatch(
            listOf(
                countryRu,
                countryUa,
                countryBe,
                countryDe,
                countryFr
            )
        )

        val savedCities = cityService.saveBatch(
            listOf(
                cityMoscow,
                cityPetersburg
            )
        )

        val placeMutaborSaved = placeService.save(placeMutabor)
        val placeGazgoldeSaved = placeService.save(placeGazgolder)
        val placePowerhouseSaved = placeService.save(placePowerhouse)
        val placeSlezySaved = placeService.save(placeSlezy)

        placeService.updateScenesOfPlace(
            placeMutaborSaved.id,
            listOf(
                sceneMutaborMain,
                sceneMutaborMedium,
                sceneMutaborGarden
            )
        )

        val scenesMutaborSaved = placeService.getAllScenes()

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
        val artistMashkovSaved = artistService.save(artistMashkov)
        val artistModelsSaved = artistService.save(artistModels)
        val artistMujuiceSaved = artistService.save(artistMujuice)
        val artistParfaitSaved = artistService.save(artistParfait)
        val artistRodinaSaved = artistService.save(artistRodina)
        val artistVillalobosSaved = artistService.save(artistVillalobos)
        val artistZotsSaved = artistService.save(artistZots)

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

        val eventSystem = EventWriteDto(
            id = null,
            name = "System One Hundred Eight",
            imageLink = "https://sun9-32.userapi.com/impf/6iUkR2dK_tKA_XrtK_uDC1S84Wi6A798V9F1sQ/5_vyoPxQnKI.jpg?size=807x422&quality=96&sign=d9392037eda29e1270fbce125735c89d&type=album",
            about = "После длительного воздержания, лесных приключений и сайд-вечеринок, команда System 108 возвращается в стены любимого завода на Дубровке. В субботнюю ночь лайнап события составят резиденты объединения, а также их друзья с лайвами и сетами. В программе ивента четыре живых выступления, которые исполнят Kovyazin D, Mujuice, Pavel Afanasyev и Philipp Gorbachev. Помимо громких лайвов, ожидаем оскароносные сеты от Chronic Preview, Egor Holkin, Errortica, Fanick, Mashkov, Nastya Zimens, Odopt, Orange и Séxstasy.",
            ticketsLink = "google.com",
            startDateTime = dateTimeProvider.getNow().plusHours(1),
            endDateTime = dateTimeProvider.getNow().plusDays(3).plusHours(12),
            ticketPrices = listOf(
                TicketPriceDto(
                    name = "One",
                    price = 1000.0,
                    currency = MoneyCurrency.RUB
                )
            ),
            placeId = placeMutaborSaved.id,
            organizers = setOf(
                unitySystemSaved.id,
                unityArmaSaved.id,
            )
        )

        val eventRadost = EventWriteDto(
            id = null,
            name = "Радость осени",
            imageLink = "https://sun9-3.userapi.com/impf/6fhybramftz3iKZyjnMYWEkuOWwGGbRlNyWjAA/YbeX05dtrnI.jpg?size=807x422&quality=96&sign=334eeab97e92f0957467dc2d6ae0bee3&type=album",
            about = "Дневная вечеринка в «Мутаборе», в рамках которой, помимо музыки, пройдут перформансы, маркет локальных дизайнеров, выставка картин и тату-сеансы. За электронное сопровождение ответят диджеи Schulz b2b Ginger, Sofia Rodina, Roman Ptashenko, Adil и GLS, а с лайвами предстанут Rat’s Eyes, Dubrovsky и Fathers sins.",
            ticketsLink = null,
            startDateTime = dateTimeProvider.getNow().minusDays(1),
            endDateTime = dateTimeProvider.getNow().minusDays(1).plusHours(5),
            ticketPrices = listOf(
                TicketPriceDto(
                    name = "One",
                    price = 500.0,
                    currency = MoneyCurrency.RUB
                )
            ),
            placeId = placeMutaborSaved.id,
            organizers = setOf(
                unityArmaSaved.id
            )
        )

        val eventCombo = EventWriteDto(
            id = null,
            name = "Комбо",
            imageLink = "https://sun9-44.userapi.com/impf/DKGPcayeA1sy1mQZS1HSudF0qBAkNLRiPuMGAA/nnkC0XLby-0.jpg?size=807x436&quality=96&sign=259845352f55d8d9cb02caebb8126d0f&type=album",
            about = "Субботняя тусовка в баре «Слёзы» ознаменуется ломовейшим локальным лайнапом. Отмечать наступление осени на одной из самых популярных веранд города будем под музыкальное сопровождение от Poima, Act x Protosoniq, Low808, Hipushit, Karolina BNV и Sofia Rodina.",
            ticketsLink = null,
            startDateTime = dateTimeProvider.getNow().minusDays(1).plusHours(5),
            endDateTime = dateTimeProvider.getNow().minusHours(5),
            ticketPrices = listOf(
                TicketPriceDto(
                    name = "После 20:00", // todo sort prices by value on GET
                    price = 500.0,
                    currency = MoneyCurrency.RUB
                ),
                TicketPriceDto(
                    name = "До 20:00",
                    price = 0.0,
                    currency = MoneyCurrency.RUB
                ),
            ),
            placeId = placeSlezySaved.id,
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
            about = "Фирменная вечеринка лейбла Hyperboloid в клубе Powerhouse. Лайнап мероприятия составили резиденты и уже знакомые многим имена локальных артистов, некоторые из которых не раз выступали на событиях импринта: Bad Zu, BOGUE x Василий Яковлев, Clear Cast, data drain, Fisky, KRBSS и zarya.",
            ticketsLink = null,
            startDateTime = dateTimeProvider.getNow(),
            endDateTime = dateTimeProvider.getNow().plusHours(8),
            ticketPrices = listOf(
                TicketPriceDto(
                    name = "Free",
                    price = 0.0,
                    currency = MoneyCurrency.RUB
                ),
            ),
            placeId = placePowerhouseSaved.id,
            organizers = setOf(
            )
        )

        val eventSanchez = EventWriteDto(
            id = null,
            name = "Sanchez Thursdays",
            imageLink = "https://sun9-70.userapi.com/impf/HcGo_gSFS9emEUabJI130FcFGgDWxS5Sv-N5wQ/FojhJi2t3_4.jpg?size=807x424&quality=96&sign=4cc06a1cbe7a106e6a0b1bcdb7fb2da1&type=album",
            about = "Традиционные четверги Санчеза в «Пропаганде».В лайнапе: Sergey Sanchez, D.A.L.I. и Sapurra.",
            ticketsLink = "google.com",
            startDateTime = dateTimeProvider.getNow(),
            endDateTime = dateTimeProvider.getNow().plusHours(8),
            ticketPrices = listOf(
                TicketPriceDto(
                    name = "Free",
                    price = 0.0,
                    currency = MoneyCurrency.RUB
                ),
                TicketPriceDto(
                    name = "После 00:00",
                    price = 1000.0,
                    currency = MoneyCurrency.RUB
                ),
                TicketPriceDto(
                    name = "После 6:00",
                    price = 500.0,
                    currency = MoneyCurrency.RUB
                ),
            ),
            placeId = placeGazgoldeSaved.id,
            organizers = setOf(
            )
        )

        val eventApplique = EventWriteDto(
            id = null,
            name = "Applique: Golden Hits",
            imageLink = "https://sun9-6.userapi.com/impf/GTw48P6DtpHtoHwsfj4jIuLtrhvuqH7FskfsJw/QBjtlCNata0.jpg?size=807x423&quality=96&sign=b7f6613d28ffed9c8e80682f47d77f6b&type=album",
            about = "Весельчаки из команды Applique возвращаются с новой тусовкой Golden Hits. Название события говорящее — там будут звучать все хаус-гимны будущего и настоящего, а также полюбившиеся шлягеры прошлых вечеринок объединения. Золотую коллекцию на тусовке будут ставить 12 диджеев, выступления многих из которых стали традиционными для ивентов от Applique. Лайнап: дуэт «Ай-нэ-нэ!», Timur Omar, Kovyazin D, Hipushit, Adamov, Kirill Shapovalov, D.A.L.I., Mark S, Levandowskiy, Розовый человек, Natali F и LDR",
            ticketsLink = "google.com",
            startDateTime = dateTimeProvider.getNow(),
            endDateTime = dateTimeProvider.getNow().plusHours(8),
            ticketPrices = listOf(
                TicketPriceDto(
                    name = "До 00:00",
                    price = 10000.0,
                    currency = MoneyCurrency.RUB
                ),
                TicketPriceDto(
                    name = "После 00:00",
                    price = 12000.0,
                    currency = MoneyCurrency.RUB
                ),
                TicketPriceDto(
                    name = "После 6:00",
                    price = 500.0,
                    currency = MoneyCurrency.RUB
                ),
            ),
            placeId = placeGazgoldeSaved.id,
            organizers = setOf(
            )
        )

        val eventSynchron = EventWriteDto(
            id = null,
            name = "Synchron",
            imageLink = "https://sun9-63.userapi.com/impf/1YN0U_HwapP6kQviE95Jg85obf41TdXsOFgPqQ/CKAql91bu94.jpg?size=807x422&quality=96&sign=ce58f9016e4f5a3fda579ad1834c995c&type=album",
            about = "Молодая столичная промокоманда Synchron покоряет новые вершины. На этот раз она объявляет вечеринку на трёх танцполах «Мутабора», где свои лайвы и сеты отыграют необычные локальные артисты из числа резидентов и друзей объединения. Лайнап мероприятия: SNS, DRIADA, Medhi Tourneur, Miroliubov & Glushkov, DBaldokhin, Salibatr Brastislavovich, Lidvall, Xandr.vasiliev, Vishnevskiy, Ratigar, Quiet Light, CPSL и многие другие имена",
            ticketsLink = "google.com",
            startDateTime = dateTimeProvider.getNow().plusDays(7),
            endDateTime = dateTimeProvider.getNow().plusDays(7).plusHours(5),
            ticketPrices = listOf(
                TicketPriceDto(
                    name = "До 00:00",
                    price = 500.0,
                    currency = MoneyCurrency.RUB
                ),
                TicketPriceDto(
                    name = "После 00:00",
                    price = 800.0,
                    currency = MoneyCurrency.RUB
                ),
            ),
            placeId = placeMutaborSaved.id,
            organizers = setOf(
            )
        )

        val eventRabitsa = EventWriteDto(
            id = null,
            name = "Рабица х НИИ",
            imageLink = "https://sun9-17.userapi.com/impf/JfHbAwbSVcc_dvkeW9fQHfGrzlTfWmlHcIAslw/XNs0T_F2AHw.jpg?size=807x367&quality=96&sign=e305c479e13c39574d10472b2af61ad0&type=album",
            about = "В последнюю субботу сентября в «Мутаборе» объединятся одни из ключевых команд на тусовочной карте города — рабы «Рабицы» и научные сотрудники из почившего клуба «НИИ». Их совместная вечеринка пройдёт на трёх танцполах клуба, где в полном составе выступят резиденты обоих уважаемых объединений. Лайнап вечеринки-коллаборации: ADIL, Burago, Buttechno, Caspian, Sergey Golikov, HMOT, Humanoid Lyubovnik, John Rock, Khamn, Low 808, MILF, Nikita Bugaev, Ranishe Niyaak, Sariim, Vtgnike и другие артисты",
            ticketsLink = "google.com",
            startDateTime = dateTimeProvider.getNow().plusDays(8),
            endDateTime = dateTimeProvider.getNow().plusDays(8).plusHours(5),
            ticketPrices = listOf(
                TicketPriceDto(
                    name = "До 00:00",
                    price = 500.0,
                    currency = MoneyCurrency.RUB
                ),
                TicketPriceDto(
                    name = "После 00:00",
                    price = 800.0,
                    currency = MoneyCurrency.RUB
                ),
                TicketPriceDto(
                    name = "После 08:00",
                    price = 0.0,
                    currency = MoneyCurrency.RUB
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
            name = "Ricardo Villalobos Mutabor",
            imageLink = "https://gotoparty.ru/public/img/upload/tmp/9053175909c4903cfa21a902b876620e.jpg",
            about = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
            ticketsLink = "google.com",
            startDateTime = dateTimeProvider.getNow().plusDays(8).plusHours(1),
            endDateTime = dateTimeProvider.getNow().plusDays(8).plusHours(9),
            ticketPrices = listOf(
                TicketPriceDto(
                    name = "До 00:00",
                    price = 500.0,
                    currency = MoneyCurrency.RUB
                ),
                TicketPriceDto(
                    name = "После 00:00",
                    price = 1800.0,
                    currency = MoneyCurrency.RUB
                ),
                TicketPriceDto(
                    name = "После 08:00",
                    price = 0.0,
                    currency = MoneyCurrency.RUB
                ),
            ),
            placeId = placeMutaborSaved.id,
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
                TicketPriceDto(
                    name = "До 00:00",
                    price = 500.0,
                    currency = MoneyCurrency.RUB
                ),
                TicketPriceDto(
                    name = "После 00:00",
                    price = 1800.0,
                    currency = MoneyCurrency.RUB
                ),
                TicketPriceDto(
                    name = "После 08:00",
                    price = 0.0,
                    currency = MoneyCurrency.RUB
                ),
            ),
            placeId = placeMutaborSaved.id,
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
                TicketPriceDto(
                    name = "До 00:00",
                    price = 500.0,
                    currency = MoneyCurrency.RUB
                ),
                TicketPriceDto(
                    name = "После 00:00",
                    price = 1800.0,
                    currency = MoneyCurrency.RUB
                ),
            ),
            placeId = placeMutaborSaved.id,
            organizers = setOf(
                unitySystemSaved.id,
                unityLenskeSaved.id,
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

        val performance2 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesMutaborSaved[0].id,
            artistIds = setOf(artistBejenecSaved.id),
            typeOfPerformance = "DJ SET",
            startingDateTime = dateTimeProvider.getNow().minusHours(1),
            endingDateTime = dateTimeProvider.getNow().minusMinutes(3)
        )

        val performance3 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesMutaborSaved[0].id,
            artistIds = setOf(artistAbelleSaved.id, artistAllienSaved.id, artistCuveSaved.id),
            typeOfPerformance = null,
            startingDateTime = dateTimeProvider.getNow().minusHours(6),
            endingDateTime = dateTimeProvider.getNow().plusMinutes(1),
        )

        val performance4 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesMutaborSaved[0].id,
            artistIds = setOf(artistAbelleSaved.id, artistChronicSaved.id),
            typeOfPerformance = null,
            startingDateTime = dateTimeProvider.getNow().minusHours(8),
            endingDateTime = dateTimeProvider.getNow().plusHours(9)
        )

        val performance5 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesMutaborSaved[0].id,
            artistIds = setOf(artistGorbachevSaved.id),
            typeOfPerformance = null,
            startingDateTime = dateTimeProvider.getNow(),
            endingDateTime = dateTimeProvider.getNow().plusHours(9).plusMinutes(1)
        )

        val performance6 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesMutaborSaved[1].id,
            artistIds = setOf(artistGorbachevSaved.id),
            typeOfPerformance = null,
            startingDateTime = dateTimeProvider.getNow().minusHours(9),
            endingDateTime = dateTimeProvider.getNow().minusHours(1)
        )

        val performance7 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesMutaborSaved[1].id,
            artistIds = setOf(artistMashkovSaved.id),
            typeOfPerformance = "Trance music",
            startingDateTime = dateTimeProvider.getNow().plusHours(1),
            endingDateTime = dateTimeProvider.getNow().plusHours(6)
        )

        val performance8 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesMutaborSaved[1].id,
            artistIds = setOf(artistLensSaved.id),
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null
        )

        val performance9 = TimetablePerformanceWriteDto(
            id = null,
            sceneId = scenesMutaborSaved[1].id,
            artistIds = setOf(artistFarragoSaved.id),
            typeOfPerformance = null,
            startingDateTime = null,
            endingDateTime = null
        )

        eventService.updateTimetableForEvent(
            eventSystemSaved.id, setOf(
                performance1,
                performance2,
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
        while (i < 15) {
            artistService.incrementFollowersUnsafe(artistMashkovSaved.id)
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

        artistService.setBestOfTheWeekForAllCities()

        artistWeeklyFollowersQuickRepo.returnAllValuesToInitial()
        unityWeeklyFollowersQuickRepo.returnAllValuesToInitial()
        placeWeeklyFollowersQuickRepo.returnAllValuesToInitial()
        eventWeeklyFollowersQuickRepo.returnAllValuesToInitial()

        i = 0
        while (i < 63) {
            eventService.incrementFollowersUnsafe(eventPaxSaved.id)
            i++
        }

        i = 0
        while (i < 74) {
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
        while (i < 8) {
            artistService.incrementFollowersUnsafe(artistAbelleSaved.id)
            i++
        }

        i = 0
        while (i < 57) {
            artistService.incrementFollowersUnsafe(artistMujuiceSaved.id)
            i++
        }

        i = 0
        while (i < 25) {
            artistService.incrementFollowersUnsafe(artistMashkovSaved.id)
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