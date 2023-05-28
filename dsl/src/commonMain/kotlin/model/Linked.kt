package model

import kotlinx.serialization.Serializable
import serialization.LinkedSerializer

@Serializable(with = LinkedSerializer::class)
sealed class Linked<T> {
    abstract val id: String
    data class Reference<T>(override val id: String): Linked<T>()
    data class Actual<T>(override val id: String, val result: T): Linked<T>()
}