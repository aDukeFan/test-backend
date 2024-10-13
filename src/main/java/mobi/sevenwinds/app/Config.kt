package mobi.sevenwinds.app

import io.ktor.config.ApplicationConfig

object Config {
    val logAllRequests by lazy { config.propertyOrNull("ktor.logAllRequests")?.getString()?.toBoolean() == true }

    private lateinit var config: ApplicationConfig

    fun init(config: ApplicationConfig) {
        this.config = config
    }
}