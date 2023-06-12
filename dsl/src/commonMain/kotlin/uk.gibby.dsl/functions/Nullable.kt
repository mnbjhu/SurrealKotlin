package uk.gibby.dsl.functions

import uk.gibby.driver.surrealJson
import kotlinx.serialization.encodeToString
import uk.gibby.dsl.types.NullableType
import uk.gibby.dsl.types.Reference


infix fun <T, U: Reference<T>> NullableType<T, U>.coalesce(default: U): U = inner.createReference("${getReference()} ?? ${default.getReference()}") as U
inline infix fun <reified T, U: Reference<T>> NullableType<T, U>.coalesce(default: T): U = inner.createReference("${getReference()} ?? ${surrealJson.encodeToString(default)}") as U
