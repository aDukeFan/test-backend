import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import mobi.sevenwinds.app.budget.BudgetResponse
import mobi.sevenwinds.app.budget.BudgetResponseWithAuthor
import mobi.sevenwinds.app.budget.BudgetResponseWithAuthorAndAuthorCreateAt
import mobi.sevenwinds.app.budget.BudgetResponseWithoutAuthor
import mobi.sevenwinds.app.budget.BudgetType

class BudgetResponseDeserializer : JsonDeserializer<BudgetResponse>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BudgetResponse {
        val node: JsonNode = p.codec.readTree(p)

        val fieldCount = node.size()

        return when (fieldCount) {
            4 -> {
                BudgetResponseWithoutAuthor(
                    node.get("year").asInt(),
                    node.get("month").asInt(),
                    node.get("amount").asInt(),
                    BudgetType.valueOf(node.get("type").asText())
                )
            }

            5 -> {
                BudgetResponseWithAuthor(
                    node.get("year").asInt(),
                    node.get("month").asInt(),
                    node.get("amount").asInt(),
                    BudgetType.valueOf(node.get("type").asText()),
                    node.get("authorName").asText()
                )
            }

            6 -> {
                BudgetResponseWithAuthorAndAuthorCreateAt(
                    node.get("year").asInt(),
                    node.get("month").asInt(),
                    node.get("amount").asInt(),
                    BudgetType.valueOf(node.get("type").asText()),
                    node.get("authorName").asText(),
                    node.get("createAtAuthor").asText()
                )
            }

            else -> throw IllegalArgumentException("Unknown BudgetResponse type")
        }
    }
}
