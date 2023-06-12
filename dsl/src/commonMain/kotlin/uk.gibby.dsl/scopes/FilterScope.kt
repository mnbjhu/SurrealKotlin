package uk.gibby.dsl.scopes

import uk.gibby.dsl.model.Linked
import uk.gibby.dsl.types.BooleanType
import uk.gibby.dsl.types.ListType
import uk.gibby.dsl.types.RecordLink
import uk.gibby.dsl.types.RecordType

interface FilterScope {
    fun getFilterString(): String
    fun where(condition: BooleanType): UnitType
    fun <T, U: RecordType<T>>fetch(items: ListType<Linked<T>, RecordLink<T, U>>): UnitType
}

interface ReturningFilterScope<T, U: RecordType<T>>: FilterScope, ReturningScope<T, U>
class ReturningFilterScopeImpl<T, U: RecordType<T>>(private val _type: U): ReturningFilterScope<T, U>, ReturningScope<T, U> by ReturningScopeImpl(_type), FilterScope by FilterScopeImpl(_type)
class SettableFilterScopeImpl<T, U: RecordType<T>>(private val _type: U): SetScope(), ReturningFilterScope<T, U>, ReturningScope<T, U> by ReturningScopeImpl(_type), FilterScope by FilterScopeImpl(_type)