package model.rows

import kotlinx.serialization.Serializable
import serialization.row.Row3Serializer

@Serializable(with = Row3Serializer::class)
data class Row3<T, U, V>(val col1: T, val col2: U, val col3: V)
