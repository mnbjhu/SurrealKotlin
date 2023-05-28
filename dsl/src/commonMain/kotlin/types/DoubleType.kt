package types

import functions.SurrealComparable
import kotlin.jvm.JvmInline

@JvmInline
value class DoubleType(private val reference: String): Reference<Double>, SurrealComparable<Double> {
    override fun getReference(): String = reference
    override fun createReference(ref: String) = DoubleType(ref)
}

val doubleType = DoubleType("_")
