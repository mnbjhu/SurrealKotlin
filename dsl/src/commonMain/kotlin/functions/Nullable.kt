package functions

import driver.surrealJson
import kotlinx.serialization.encodeToString
import types.NullableType
import types.Reference


infix fun <T, U: Reference<T>> NullableType<T, U>.coalesce(default: U): U = inner.createReference("${getReference()} ?? ${default.getReference()}") as U
inline infix fun <reified T, U: Reference<T>> NullableType<T, U>.coalesce(default: T): U = inner.createReference("${getReference()} ?? ${surrealJson.encodeToString(default)}") as U
