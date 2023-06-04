@file:Suppress("UNCHECKED_CAST")

package core

import driver.surrealJson
import kotlinx.serialization.encodeToString
import types.Reference


inline infix fun <reified T, U: Reference<T>>U.of(value: T): U = createReference(surrealJson.encodeToString(value)) as U