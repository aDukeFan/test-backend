package mobi.sevenwinds.modules

import com.papsign.ktor.openapigen.openAPIGen
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.tag
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.get
import mobi.sevenwinds.app.author.author
import mobi.sevenwinds.app.budget.budget

fun NormalOpenAPIRoute.swaggerRouting() {
    tag(SwaggerTag.Бюджет) { budget() }
    tag(SwaggerTag.Автор) { author() }
}

fun Routing.serviceRouting() {
    get("/") {
        call.respondRedirect("/swagger-ui/index.html?url=/openapi.json", true)
    }

    get("/openapi.json") {
        call.respond(application.openAPIGen.api.serialize())
    }
}