package mobi.sevenwinds.app.author

import kotlinx.coroutines.runBlocking
import mobi.sevenwinds.app.author.AuthorService.addRecord
import mobi.sevenwinds.common.ServerTest
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthorApiKtTest : ServerTest() {

    @BeforeEach
    internal fun setUp() {
        transaction { AuthorTable.deleteAll() }
    }

    @Test
    fun testAddAuthor() = runBlocking {
        val newAuthor = AuthorRecord(fullName = "Mr Freeman")
        val authorResponse = addRecord(AuthorRecord(fullName = "Mr Freeman"))
        assertNotNull(authorResponse)
        assertEquals(newAuthor.fullName, authorResponse.fullName)
        assertNotNull(authorResponse.createdAt)

        transaction {
            val savedAuthor = AuthorTable.select { AuthorTable.fullName eq newAuthor.fullName }.singleOrNull()
            assertNotNull(savedAuthor)
            assertEquals(newAuthor.fullName, savedAuthor[AuthorTable.fullName])
        }
    }
}