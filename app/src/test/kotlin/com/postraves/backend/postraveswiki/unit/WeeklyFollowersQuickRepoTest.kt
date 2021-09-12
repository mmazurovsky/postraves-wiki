package com.postraves.backend.postraveswiki.unit

import com.postraves.backend.postraveswiki.config.logger
import com.postraves.backend.postraveswiki.repo.quick.CleaningQuickRepo
import com.postraves.backend.postraveswiki.repo.quick.WeeklyFollowersQuickRepo
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import redis.embedded.RedisServer
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles(value = ["test"])
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = ["spring.flyway.enabled=false"])
class WeeklyFollowersQuickRepoTest(
    @Value("\${spring.redis.port}")
    redisPort: Int,
    @Autowired
    private val quickRepoCleaning: CleaningQuickRepo,
    @Autowired @Qualifier("artistWeeklyFollowersQuickRepoImpl")
    private val weeklyFollowersQuickRepo: WeeklyFollowersQuickRepo,
) {

    private val redisServer = RedisServer(redisPort)

    init {
        redisServer.start()
    }

    companion object {
        private const val defaultEntityId: Long = 1
    }

    @BeforeAll
    private fun clearAllData() {
        logger.info("Weekly Followers Unit Test started")
        quickRepoCleaning.clearAllData()
    }

    @AfterAll
    private fun clearAll() {
        quickRepoCleaning.clearAllData()
        redisServer.stop()
        logger.info("Weekly Followers Unit Test ended")
    }

    private fun doInWhileLoop(
        numberOfOperations: Int,
        entityId: Long,
        func: (Long) -> Int
    ) {
        var i = 0
        while (i != numberOfOperations) {
            i++
            func(entityId)
        }
    }

    private val lambdaIncrement = { entityId: Long ->
        weeklyFollowersQuickRepo.incrementFollowers(
            entityId
        )
    }

    private val lambdaDecrement = { entityId: Long ->
        weeklyFollowersQuickRepo.decrementFollowers(
            entityId
        )
    }

    @Test
    @Order(1)
    fun setInitialFollowersDeltaForEntityAndGetIt() {
        weeklyFollowersQuickRepo.setInitialFollowers(defaultEntityId)
        val result = weeklyFollowersQuickRepo.getFollowers(defaultEntityId)
        assertEquals(0, result)
    }

    @Test
    @Order(2)
    fun incrementFollowersDeltaAndGetIt() {
        weeklyFollowersQuickRepo.incrementFollowers(defaultEntityId)
        val result = weeklyFollowersQuickRepo.getFollowers(defaultEntityId)
        assertEquals(1, result)
    }

    @Test
    @Order(3)
    fun decrementFollowersDeltaAndGetIt() {
        weeklyFollowersQuickRepo.decrementFollowers(defaultEntityId)
        val result = weeklyFollowersQuickRepo.getFollowers(defaultEntityId)
        assertEquals(0, result)
    }

    @Test
    @Order(4)
    fun incrementFollowersDeltaMultipleTimesAndGetIt() {
        doInWhileLoop(5, defaultEntityId, lambdaIncrement)
        val result = weeklyFollowersQuickRepo.getFollowers(defaultEntityId)
        assertEquals(5, result)
    }

    @Test
    @Order(5)
    fun decrementFollowersDeltaMultipleTimesAndGetIt() {
        doInWhileLoop(10, defaultEntityId, lambdaDecrement)
        val result = weeklyFollowersQuickRepo.getFollowers(defaultEntityId)
        assertEquals(-5, result)
    }

    @Test
    @Order(6)
    fun addNewEntitiesIncrementSomeOfThemAndGetTop() {
//        weeklyFollowersRepo.setInitialFollowers(2)
//        weeklyFollowersRepo.setInitialFollowers(3)
//        weeklyFollowersRepo.setInitialFollowers(4)
//        weeklyFollowersRepo.setInitialFollowers(5)
        val entityId2: Long = 2
        val entityId3: Long = 3
        val entityId4: Long = 4
        val entityId5: Long = 5

        doInWhileLoop(10, entityId2, lambdaDecrement)
        doInWhileLoop(1, entityId3, lambdaIncrement)
        doInWhileLoop(2, entityId4, lambdaIncrement)
        doInWhileLoop(10, entityId5, lambdaIncrement)


        val result = weeklyFollowersQuickRepo.findTop(50)
        assertEquals(5, result.size)
        var mapIterationIndex = -1
        result.forEach {
            mapIterationIndex++
            when (mapIterationIndex) {
                0 -> {
                    assertEquals(5, it.key)
                    assertEquals(10, it.value)
                }
                1 -> {
                    assertEquals(4, it.key)
                    assertEquals(2, it.value)
                }
                2 -> {
                    assertEquals(3, it.key)
                    assertEquals(1, it.value)
                }
                3 -> {
                    assertEquals(1, it.key)
                    assertEquals(-5, it.value)
                }
                4 -> {
                    assertEquals(2, it.key)
                    assertEquals(-10, it.value)
                }
            }
        }
    }

    @Test
    @Order(7)
    fun returnAllToInitial() {
        weeklyFollowersQuickRepo.returnAllValuesToInitial()

        val result = weeklyFollowersQuickRepo.findTop(50)
        assertEquals(5, result.size)
        result.forEach {
            assertEquals(0, it.value)
        }
    }

    @Test
    @Order(8)
    fun getNotExistingValue() {
        val entityIdNonExisting: Long = 10
        val result = weeklyFollowersQuickRepo.getFollowers(entityIdNonExisting)
        assertEquals(0, result)
    }

    @Test
    @Order(9)
    fun tryToIncrementNotExistingValue() {
        val entityIdNonExisting: Long = 11
        val result = weeklyFollowersQuickRepo.incrementFollowers(entityIdNonExisting)
        assertEquals(1, result)
    }
}