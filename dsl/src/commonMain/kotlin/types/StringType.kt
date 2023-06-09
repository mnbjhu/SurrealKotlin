package types

import kotlin.jvm.JvmInline

@JvmInline
value class StringType(private val reference: String): Reference<String>, SurrealComparable<String> {
    override fun getReference(): String = reference
    override fun createReference(ref: String): Reference<String> = StringType(ref)
}
val stringType = StringType("_")

