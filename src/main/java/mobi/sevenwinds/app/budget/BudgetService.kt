package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetResponse = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId?.let { EntityID(it, AuthorTable) }
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = if (param.authorName != null) {
                BudgetTable
                    .join(AuthorTable, JoinType.INNER)
                    .select {
                        BudgetTable.year eq param.year and
                                (AuthorTable.fullName.lowerCase()
                                        like "%${param.authorName}%".toLowerCase())
                        // Фильтрация по ФИО я бы использовал iLike, но видимо зависимости в проекте устаревшие
                    }
                    .limit(param.limit, param.offset)
            } else {
                BudgetTable
                    .select { BudgetTable.year eq param.year }
                    .limit(param.limit, param.offset)
            }

            val total = query.count()

            // добавил сортировку согласно ТЗ
            val data = BudgetEntity.wrapRows(query).map { it.toResponse(includeAuthorCreatedAt = true) }
                .sortedWith(compareBy({ it.month }, { -it.amount }))

            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}