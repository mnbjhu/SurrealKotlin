package model.rows

import kotlinx.serialization.Serializable
import serialization.row.Row2Serializer

@Serializable(with = Row2Serializer::class)
data class Row2<A, B>(val col1: A, val col2: B)