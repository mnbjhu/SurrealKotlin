package driver

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

actual fun getEngine(): HttpClientEngineFactory<*> = Darwin