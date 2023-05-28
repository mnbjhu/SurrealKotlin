package core

import scopes.*
import types.*
import uk.gibby.dsl.core.TableId
import types.row.RowType

data class Table<T, U: RecordType<T>>(val name: String, val recordType: U) {
    fun delete(): ListType<String?, NullableType<String, StringType>> {
        return ListType(NullableType("_", stringType), "(DELETE FROM $name)")
    }
    fun <a, A: Reference<a>>delete(deleteScope: ReturningFilterScope<T, U>.(U) -> A): ListType<String?, NullableType<String, StringType>> {
        val filter = ReturningFilterScopeImpl(recordType)
        val returned = with(filter) { deleteScope(recordType) }
        return ListType(NullableType("_", stringType), "(DELETE FROM $name${filter.getFilterString()} RETURN ${returned.getReference()})")
    }

    fun selectAll(selectScope: FilterScope.(U) -> Unit = {}): ListType<T, U> {
        val filter = FilterScopeImpl(recordType)
        with(filter) { selectScope(recordType) }
        return ListType(recordType, "(SELECT * FROM $name${filter.getFilterString()})")
    }

    fun <r, R: Reference<r>>select(projection: FilterScope.(U) -> R): ListType<r, R> {
        val filter = FilterScopeImpl(recordType)
        val toSelect = with(filter) {
            projection(recordType)
        }
        return ListType(
            toSelect,
            "(SELECT ${if (toSelect is RowType) "" else "VALUE " }${toSelect.getReference()} FROM $name${filter.getFilterString()})"
        )
    }

    fun <a, A: Reference<a>>update(updateScope: SettableFilterScopeImpl<T, U>.(U) -> A): ListType<a, A> {
        val scope = SettableFilterScopeImpl(recordType)
        val returned = scope.updateScope(recordType)
        return ListType(returned, "UPDATE $name ${scope.getSetString()} ${scope.getFilterString()} " +
                "RETURN ${when(val ref = returned.getReference()){
                    "AFTER" -> "AFTER"
                    "BEFORE" -> "BEFORE"
                    "NONE" -> "NONE"
                    else -> { if(returned is RowType) ref else "VALUE $ref" }
                }}")
    }
    operator fun get(id: String): TableId<T, U> {
        return TableId("$name:$id", recordType)
    }
}

