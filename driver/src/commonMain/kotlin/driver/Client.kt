package driver

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.json.Json

val Client = HttpClient(getEngine()) {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json { ignoreUnknownKeys = true })
    }
}

expect fun getEngine(): HttpClientEngineFactory<*>

val surrealJson = Json {
    classDiscriminator = "status"
    ignoreUnknownKeys = true
}