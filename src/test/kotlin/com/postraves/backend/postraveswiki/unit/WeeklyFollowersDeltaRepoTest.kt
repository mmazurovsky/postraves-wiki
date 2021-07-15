package com.postraves.backend.postraveswiki.unit

import com.postraves.backend.postraveswiki.repo.WeeklyFollowersDeltaRepoImpl
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import redis.embedded.RedisServer
import kotlin.test.assertEquals


@SpringBootTest
@ActiveProfiles(value = ["test"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = ["spring.flyway.enabled=false"])
class WeeklyFollowersDeltaRepoTest(
    @Value("\${spring.redis.port}") redisPort: Int,
    @Autowired private val weeklyFollowersDeltaRepo: WeeklyFollowersDeltaRepoImpl,
) {

    private var redisServer: RedisServer = RedisServer(redisPort)

    init {
        redisServer.start()
    }

    companion object {
        private const val entityType = "artist"
        private const val defaultEntityId: Long = 1
    }

    @BeforeAll
    private fun clearData() {
        weeklyFollowersDeltaRepo.clearAllData()
    }

    @AfterAll
    private fun stopRedis() {
        weeklyFollowersDeltaRepo.clearAllData()
        redisServer.stop()
    }

    private fun doInWhileLoop(
        numberOfOperations: Int,
        entityType: String,
        entityId: Long,
        func: (String, Long) -> Int
    ) {
        var i = 0
        while (i != numberOfOperations) {
            i++
            func(entityType, entityId)
        }
    }

    private val lambdaIncrement = { entityType: String, entityId: Long ->
        weeklyFollowersDeltaRepo.incrementWeeklyFollowersDelta(
            entityType, entityId
        )
    }

    private val lambdaDecrement = { entityType: String, entityId: Long ->
        weeklyFollowersDeltaRepo.decrementWeeklyFollowersDelta(
            entityType, entityId
        )
    }

    @Test
    @Order(1)
    fun setInitialFollowersDeltaForEntityAndGetIt() {
        weeklyFollowersDeltaRepo.setInitialWeeklyFollowersDelta(entityType, defaultEntityId)
        val result = weeklyFollowersDeltaRepo.getWeeklyFollowersDelta(entityType, defaultEntityId)
        assertEquals(0, result)
    }

    @Test
    @Order(2)
    fun incrementFollowersDeltaAndGetIt() {
        weeklyFollowersDeltaRepo.incrementWeeklyFollowersDelta(entityType, defaultEntityId)
        val result = weeklyFollowersDeltaRepo.getWeeklyFollowersDelta(entityType, defaultEntityId)
        assertEquals(1, result)
    }

    @Test
    @Order(3)
    fun decrementFollowersDeltaAndGetIt() {
        weeklyFollowersDeltaRepo.decrementWeeklyFollowersDelta(entityType, defaultEntityId)
        val result = weeklyFollowersDeltaRepo.getWeeklyFollowersDelta(entityType, defaultEntityId)
        assertEquals(0, result)
    }

    @Test
    @Order(4)
    fun incrementFollowersDeltaMultipleTimesAndGetIt() {
        doInWhileLoop(5, entityType, defaultEntityId, lambdaIncrement)
        val result = weeklyFollowersDeltaRepo.getWeeklyFollowersDelta(entityType, defaultEntityId)
        assertEquals(5, result)
    }

    @Test
    @Order(5)
    fun decrementFollowersDeltaMultipleTimesAndGetIt() {
        doInWhileLoop(10, entityType, defaultEntityId, lambdaDecrement)
        val result = weeklyFollowersDeltaRepo.getWeeklyFollowersDelta(entityType, defaultEntityId)
        assertEquals(-5, result)
    }

    @Test
    @Order(6)
    fun addNewEntitiesIncrementSomeOfThemAndGetTop() {
        weeklyFollowersDeltaRepo.setInitialWeeklyFollowersDelta(entityType, 2)
        weeklyFollowersDeltaRepo.setInitialWeeklyFollowersDelta(entityType, 3)
        weeklyFollowersDeltaRepo.setInitialWeeklyFollowersDelta(entityType, 4)
        weeklyFollowersDeltaRepo.setInitialWeeklyFollowersDelta(entityType, 5)

        doInWhileLoop(10, entityType, 2, lambdaDecrement)
        doInWhileLoop(1, entityType, 3, lambdaIncrement)
        doInWhileLoop(2, entityType, 4, lambdaIncrement)
        doInWhileLoop(10, entityType, 5, lambdaIncrement)


        val result = weeklyFollowersDeltaRepo.getTop(entityType, 50)
        assertEquals(5, result.size)
        result.forEachIndexed { index, map ->
            when (index) {
                0 -> {
                    assertEquals(5, map.keys.first())
                    assertEquals(10, map.values.first())
                }
                1 -> {
                    assertEquals(4, map.keys.first())
                    assertEquals(2, map.values.first())
                }
                2 -> {
                    assertEquals(3, map.keys.first())
                    assertEquals(1, map.values.first())
                }
                3 -> {
                    assertEquals(1, map.keys.first())
                    assertEquals(-5, map.values.first())
                }
                4 -> {
                    assertEquals(2, map.keys.first())
                    assertEquals(-10, map.values.first())
                }
            }
        }
    }

    @Test
    @Order(7)
    fun returnAllToInitial() {
        weeklyFollowersDeltaRepo.returnAllValuesToInitial(entityType)

        val result = weeklyFollowersDeltaRepo.getTop(entityType, 50)
        assertEquals(5, result.size)
        result.forEach { map ->
            assertEquals(0, map.values.first())
        }
    }
}