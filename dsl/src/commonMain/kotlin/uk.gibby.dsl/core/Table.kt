package uk.gibby.dsl.core

import uk.gibby.dsl.types.RecordType


data class Table<T, U: RecordType<T>>(val name: String, val recordType: U)

