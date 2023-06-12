package uk.gibby.dsl.core

import uk.gibby.dsl.scopes.*
import uk.gibby.dsl.types.*


class TableId<T, U: RecordType<T>>(reference: String, inner: U): RecordLink<T, U>(reference, inner) {

    override fun createReference(ref: String): TableId<T, U> {
        return TableId(ref, inner)
    }

    fun delete(): ListType<String?, NullableType<String, StringType>> {
        return ListType(NullableType("_", stringType), "DELETE FROM ${getReference()}")
    }
    fun <a, A: Reference<a>>delete(deleteScope: ReturningFilterScope<T, U>.(U) -> A): ListType<String?, NullableType<String, StringType>> {
        val filter = ReturningFilterScopeImpl(inner)
        val returned = filter.deleteScope(inner)
        return ListType(
            NullableType("_", stringType),
            "DELETE FROM ${getReference()}${filter.getFilterString()} RETURN ${returned.getReference()}"
        )
    }

    fun selectAll(selectScope: FilterScope.(U) -> Unit = {}): U {
        val filter = FilterScopeImpl(inner)
        with(filter) { selectScope(inner) }
        return inner.createReference("(SELECT * FROM ${getReference()}${filter.getFilterString()})") as U
    }

    fun <r, R: Reference<r>>select(projection: FilterScope.(U) -> R): R {
        val filter = FilterScopeImpl(inner)
        val toSelect = with(filter) {
            projection(inner)
        }
        return toSelect.createReference("(SELECT VALUE ${toSelect.getReference()} FROM ${getReference()}${filter.getFilterString()})") as R
    }

    fun <a, A: Reference<a>>update(updateScope:  SettableFilterScopeImpl<T, U>.(U) -> A): A {
        val scope = SettableFilterScopeImpl(inner)
        val returned = updateScope(scope, inner)
        return returned.createReference("UPDATE ${getReference()}{setScope.getSetString()} ${scope.getFilterString()} RETURN ${returned.getReference()}") as A
    }
}