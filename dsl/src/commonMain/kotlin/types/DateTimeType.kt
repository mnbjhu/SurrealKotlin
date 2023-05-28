package types

import kotlinx.datetime.Instant
import functions.SurrealComparable
import kotlin.jvm.JvmInline

@JvmInline
value class DateTimeType(private val reference: String): Reference<Instant>, SurrealComparable<Instant> {
    override fun getReference(): String = reference
    override fun createReference(ref: String) = DateTimeType(ref)
}

val dateTimeType = DateTimeType("_")


val durationType = DurationType("_")
