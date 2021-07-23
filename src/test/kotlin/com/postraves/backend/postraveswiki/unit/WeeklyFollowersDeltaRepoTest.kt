//package com.postraves.backend.postraveswiki.unit
//
//import com.postraves.backend.postraveswiki.repo.WeeklyFollowersDeltaRepoImpl
//import org.junit.jupiter.api.*
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.context.ActiveProfiles
//import org.springframework.test.context.TestPropertySource
//import redis.embedded.RedisServer
//import kotlin.test.assertEquals
//
//
//@SpringBootTest
//@ActiveProfiles(value = ["test"])
//@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestPropertySource(properties = ["spring.flyway.enabled=false"])
//class WeeklyFollowersDeltaRepoTest(
//    @Value("\${spring.redis.port}") redisPort: Int,
//    @Autowired private val weeklyFollowersDeltaRepo: WeeklyFollowersDeltaRepoImpl,
//) {
//
//    private val redisServer = RedisServer(redisPort)
//    init {
//        redisServer.start()
//    }
//
//    companion object {
//        private const val entityType = "artist"
//        private const val countryName = "be"
//        private const val defaultEntityId: Long = 1
//    }
//
//    @BeforeAll
//    private fun clearData() {
//        weeklyFollowersDeltaRepo.clearAllData()
//    }
//
//    @AfterAll
//    private fun stopRedis() {
//        weeklyFollowersDeltaRepo.clearAllData()
//        redisServer.stop()
//    }
//
//    private fun doInWhileLoop(
//        numberOfOperations: Int,
//        entityType: String,
//        countryName: String,
//        entityId: Long,
//        func: (String, String, Long) -> Int
//    ) {
//        var i = 0
//        while (i != numberOfOperations) {
//            i++
//            func(entityType, countryName, entityId)
//        }
//    }
//
//    private val lambdaIncrement = { entityType: String, countryName: String, entityId: Long ->
//        weeklyFollowersDeltaRepo.incrementWeeklyFollowersDelta(
//            entityType, countryName, entityId
//        )
//    }
//
//    private val lambdaDecrement = { entityType: String, countryName: String, entityId: Long ->
//        weeklyFollowersDeltaRepo.decrementWeeklyFollowersDelta(
//            entityType, countryName, entityId
//        )
//    }
//
//    @Test
//    @Order(1)
//    fun setInitialFollowersDeltaForEntityAndGetIt() {
//        weeklyFollowersDeltaRepo.setInitialWeeklyFollowersDelta(entityType, countryName, defaultEntityId)
//        val result = weeklyFollowersDeltaRepo.getWeeklyFollowersDelta(entityType, countryName, defaultEntityId)
//        assertEquals(0, result)
//    }
//
//    @Test
//    @Order(2)
//    fun incrementFollowersDeltaAndGetIt() {
//        weeklyFollowersDeltaRepo.incrementWeeklyFollowersDelta(entityType, countryName, defaultEntityId)
//        val result = weeklyFollowersDeltaRepo.getWeeklyFollowersDelta(entityType, countryName, defaultEntityId)
//        assertEquals(1, result)
//    }
//
//    @Test
//    @Order(3)
//    fun decrementFollowersDeltaAndGetIt() {
//        weeklyFollowersDeltaRepo.decrementWeeklyFollowersDelta(entityType, countryName, defaultEntityId)
//        val result = weeklyFollowersDeltaRepo.getWeeklyFollowersDelta(entityType, countryName, defaultEntityId)
//        assertEquals(0, result)
//    }
//
//    @Test
//    @Order(4)
//    fun incrementFollowersDeltaMultipleTimesAndGetIt() {
//        doInWhileLoop(5, entityType, countryName, defaultEntityId, lambdaIncrement)
//        val result = weeklyFollowersDeltaRepo.getWeeklyFollowersDelta(entityType, countryName, defaultEntityId)
//        assertEquals(5, result)
//    }
//
//    @Test
//    @Order(5)
//    fun decrementFollowersDeltaMultipleTimesAndGetIt() {
//        doInWhileLoop(10, entityType, countryName, defaultEntityId, lambdaDecrement)
//        val result = weeklyFollowersDeltaRepo.getWeeklyFollowersDelta(entityType, countryName, defaultEntityId)
//        assertEquals(-5, result)
//    }
//
//    @Test
//    @Order(6)
//    fun addNewEntitiesIncrementSomeOfThemAndGetTop() {
//        weeklyFollowersDeltaRepo.setInitialWeeklyFollowersDelta(entityType, countryName, 2)
//        weeklyFollowersDeltaRepo.setInitialWeeklyFollowersDelta(entityType, countryName, 3)
//        weeklyFollowersDeltaRepo.setInitialWeeklyFollowersDelta(entityType, countryName, 4)
//        weeklyFollowersDeltaRepo.setInitialWeeklyFollowersDelta(entityType, countryName, 5)
//
//        doInWhileLoop(10, entityType, countryName, 2, lambdaDecrement)
//        doInWhileLoop(1, entityType, countryName, 3, lambdaIncrement)
//        doInWhileLoop(2, entityType, countryName, 4, lambdaIncrement)
//        doInWhileLoop(10, entityType, countryName, 5, lambdaIncrement)
//
//
//        val result = weeklyFollowersDeltaRepo.findWeeklyTopInCountry(entityType, countryName, 50)
//        assertEquals(5, result.size)
//        var mapIterationIndex = -1
//        result.forEach {
//            mapIterationIndex++
//            when (mapIterationIndex) {
//                0 -> {
//                    assertEquals(5, it.key)
//                    assertEquals(10, it.value)
//                }
//                1 -> {
//                    assertEquals(4, it.key)
//                    assertEquals(2, it.value)
//                }
//                2 -> {
//                    assertEquals(3, it.key)
//                    assertEquals(1, it.value)
//                }
//                3 -> {
//                    assertEquals(1, it.key)
//                    assertEquals(-5, it.value)
//                }
//                4 -> {
//                    assertEquals(2, it.key)
//                    assertEquals(-10, it.value)
//                }
//            }
//        }
//    }
//
//    @Test
//    @Order(7)
//    fun returnAllToInitial() {
//        weeklyFollowersDeltaRepo.returnAllValuesToInitial(entityType, countryName)
//
//        val result = weeklyFollowersDeltaRepo.findWeeklyTopInCountry(entityType, countryName, 50)
//        assertEquals(5, result.size)
//        result.forEach {
//            assertEquals(0, it.value)
//        }
//    }
//
//    @Test
//    @Order(8)
//    fun getNotExistingValue() {
//        val result = weeklyFollowersDeltaRepo.getWeeklyFollowersDelta(entityType, countryName, 10)
//        assertEquals(0, result)
//    }
//
//    @Test
//    @Order(9)
//    fun tryToIncrementNotExistingValue() {
//        val result = weeklyFollowersDeltaRepo.incrementWeeklyFollowersDelta(entityType, countryName, 10)
//        assertEquals(1, result)
//    }
//}