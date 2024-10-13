package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BudgetApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { BudgetTable.deleteAll() }
        transaction { AuthorTable.deleteAll() }
    }

    @Test
    @DisplayName("В тесте были напутаны цифры исправлен")
    fun testBudgetPagination() {
        addRecord(BudgetRecord(2020, 5, 10, BudgetType.Приход, null))
        addRecord(BudgetRecord(2020, 5, 5, BudgetType.Приход, null))
        addRecord(BudgetRecord(2020, 5, 20, BudgetType.Приход, null))
        addRecord(BudgetRecord(2020, 5, 30, BudgetType.Приход, null))
        addRecord(BudgetRecord(2020, 5, 40, BudgetType.Приход, null))
        addRecord(BudgetRecord(2030, 1, 1, BudgetType.Расход, null))

        RestAssured.given()
            .queryParam("limit", 3)
            .queryParam("offset", 1)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType}")
                Assertions.assertEquals(3, response.total)
                Assertions.assertEquals(3, response.items.size)
                Assertions.assertEquals(55, response.totalByType[BudgetType.Приход.name])
            }
    }

    @Test
    @DisplayName("Stats Sort: expected sort order - month ascending, amount descending")
    fun testStatsSortOrder() {
        addRecord(BudgetRecord(2020, 5, 100, BudgetType.Приход, null))
        addRecord(BudgetRecord(2020, 1, 5, BudgetType.Приход, null))
        addRecord(BudgetRecord(2020, 5, 50, BudgetType.Приход, null))
        addRecord(BudgetRecord(2020, 1, 30, BudgetType.Приход, null))
        addRecord(BudgetRecord(2020, 5, 400, BudgetType.Приход, null))

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println(response.items)

                Assertions.assertEquals(30, response.items[0].amount)
                Assertions.assertEquals(5, response.items[1].amount)
                Assertions.assertEquals(400, response.items[2].amount)
                Assertions.assertEquals(100, response.items[3].amount)
                Assertions.assertEquals(50, response.items[4].amount)
            }
    }

    @Test
    fun testStatsSortWithAuthors() {
        transaction {
            AuthorTable.insert { it[fullName] = "Mr First" }
            AuthorTable.insert { it[fullName] = "Mr Second" }
        }

        addRecord(BudgetRecord(2020, 5, 10, BudgetType.Приход, 1))
        addRecord(BudgetRecord(2020, 5, 5, BudgetType.Приход, 2))
        addRecord(BudgetRecord(2020, 5, 20, BudgetType.Приход, 1))
        addRecord(BudgetRecord(2020, 5, 30, BudgetType.Приход, null))

        RestAssured.given()
            .queryParam("limit", 100)
            .queryParam("offset", 0)
            .queryParam("authorName", "first")
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println(response.items)
                Assertions.assertEquals(20, response.items[0].amount)
                Assertions.assertEquals(10, response.items[1].amount)
            }
    }

    @Test
    fun testInvalidMonthValues() {
        RestAssured.given()
            .jsonBody(BudgetRecord(2020, -5, 5, BudgetType.Приход, null))
            .post("/budget/add")
            .then().statusCode(400)

        RestAssured.given()
            .jsonBody(BudgetRecord(2020, 15, 5, BudgetType.Приход, null))
            .post("/budget/add")
            .then().statusCode(400)
    }

    private fun addRecord(record: BudgetRecord) {
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetResponse>().let { response ->
                Assertions.assertEquals(record.year, response.year)
                Assertions.assertEquals(record.month, response.month)
                Assertions.assertEquals(record.amount, response.amount)
                Assertions.assertEquals(record.type, response.type)
            }
    }
}