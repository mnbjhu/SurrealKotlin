package types

import kotlin.jvm.JvmInline
import kotlin.time.Duration

@JvmInline
value class DurationType(private val reference: String): Reference<Duration> {
    override fun getReference(): String = reference
    override fun createReference(ref: String) = DurationType(ref)
}