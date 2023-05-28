package driver

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual fun getEngine(): HttpClientEngineFactory<*> {
    return CIO
}