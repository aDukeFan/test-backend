package mobi.sevenwinds.app.budget

import mobi.sevenwinds.Const.dateTimeFormatPattern
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.joda.time.format.DateTimeFormat

object BudgetTable : IntIdTable("budget") {
    val year = integer("year")
    val month = integer("month")
    val amount = integer("amount")
    val type = enumerationByName("type", 100, BudgetType::class)
    val authorId = reference("author_id", AuthorTable).nullable()
}

class BudgetEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BudgetEntity>(BudgetTable)

    var year by BudgetTable.year
    var month by BudgetTable.month
    var amount by BudgetTable.amount
    var type by BudgetTable.type
    var authorId by BudgetTable.authorId

    fun toResponse(includeAuthorCreatedAt: Boolean = false): BudgetResponse {
        val author = authorId?.let { AuthorEntity[it] }

        return if (author != null) {
            if (includeAuthorCreatedAt) {
                BudgetResponseWithAuthorAndAuthorCreateAt(
                    year,
                    month,
                    amount,
                    type,
                    authorName = author.fullName,
                    createAtAuthor = author.createdAt.toString(DateTimeFormat.forPattern(dateTimeFormatPattern))
                )
            } else {
                BudgetResponseWithAuthor(
                    year,
                    month,
                    amount,
                    type,
                    authorName = author.fullName
                )
            }
        } else {
            BudgetResponseWithoutAuthor(
                year,
                month,
                amount,
                type
            )
        }
    }
}
