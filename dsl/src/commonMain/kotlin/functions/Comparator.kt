package functions

import driver.surrealJson
import kotlinx.serialization.encodeToString
import types.SurrealComparable
import types.booleanType


infix fun <T> SurrealComparable<T>.lessThan(other: SurrealComparable<T>) = booleanType.createReference("(${getReference()} < ${other.getReference()})")
infix fun <T> SurrealComparable<T>.greaterThan(other: SurrealComparable<T>) = booleanType.createReference("(${getReference()} > ${other.getReference()})")
infix fun <T> SurrealComparable<T>.lessThanOrEqualTo(other: SurrealComparable<T>) = booleanType.createReference("(${getReference()} <= ${other.getReference()})")
infix fun <T> SurrealComparable<T>.greaterThanOrEqualTo(other: SurrealComparable<T>) = booleanType.createReference("(${getReference()} >= ${other.getReference()})")

inline infix fun <reified T> SurrealComparable<T>.lessThan(other: T) = booleanType.createReference("(${getReference()} < ${surrealJson.encodeToString(other)})")
inline infix fun <reified T> SurrealComparable<T>.greaterThan(other: T) = booleanType.createReference("(${getReference()} > ${surrealJson.encodeToString(other)})")
inline infix fun <reified T> SurrealComparable<T>.lessThanOrEqualTo(other: T) = booleanType.createReference("(${getReference()} <= ${surrealJson.encodeToString(other)})")
inline infix fun <reified T> SurrealComparable<T>.greaterThanOrEqualTo(other: T) = booleanType.createReference("(${getReference()} >= ${surrealJson.encodeToString(other)})")

inline infix fun <reified T> T.lessThan(other: SurrealComparable<T>) = booleanType.createReference("(${surrealJson.encodeToString(this)} < ${other.getReference()})")
inline infix fun <reified T> T.greaterThan(other: SurrealComparable<T>) = booleanType.createReference("(${surrealJson.encodeToString(this)} > ${other.getReference()})")
inline infix fun <reified T> T.lessThanOrEqualTo(other: SurrealComparable<T>) = booleanType.createReference("(${surrealJson.encodeToString(this)} <= ${other.getReference()})")
inline infix fun <reified T> T.greaterThanOrEqualTo(other: SurrealComparable<T>) = booleanType.createReference("(${surrealJson.encodeToString(this)} >= ${other.getReference()})")


