package types

import kotlin.jvm.JvmInline

@JvmInline
value class LongType(private val reference: String): Reference<Long>, SurrealComparable<Long> {
    override fun getReference(): String = reference
    override fun createReference(ref: String) = LongType(ref)
}
val longType = LongType("_")
