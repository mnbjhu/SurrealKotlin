package uk.gibby.dsl.scopes

import kotlinx.serialization.encodeToString
import uk.gibby.driver.surrealJson
import uk.gibby.dsl.core.Table
import uk.gibby.dsl.core.TableId
import uk.gibby.dsl.types.*
import uk.gibby.dsl.types.row.RowType
import kotlin.reflect.KProperty


class TransactionScope {
    private var generated: String = "BEGIN TRANSACTION;\n"

    operator fun Reference<*>.unaryPlus(){
        generated += "RETURN ${getReference()};\n"
    }

    fun _getQueryText() = generated + "COMMIT TRANSACTION;"

    operator fun <T, U: Reference<T>>U.getValue(thisRef: Any?, property: KProperty<*>): U =
        createReference("\$${property.name}")
            .also { generated += "LET \$${property.name} = ${getReference()};\n" }
                as U

    inline fun <reified T, U: RecordType<T>> Table<T, U>.createContent(content: T): U =
        recordType.createReference("CREATE $name CONTENT ${surrealJson.encodeToString(content)}") as U

    inline fun <reified T, U: RecordType<T>, a, A: Reference<a>> Table<T, U>.createContent(content: T, crossinline toReturn: U.() -> A): A {
        val returned = toReturn(recordType)
        return returned.createReference("CREATE $name CONTENT ${surrealJson.encodeToString(content)} RETURN VALUE ${returned.getReference()}") as A
    }

    fun <T, U: RecordType<T>> Table<T, U>.create(scope: SetScope.(U) -> Unit): U {
        val setScope = SetScope()
        scope(setScope, recordType)
        return recordType.createReference("CREATE $name ${setScope.getSetString()}") as U
    }

    fun <T, U: RecordType<T>> TableId<T, U>.create(scope: SetScope.(U) -> Unit): U {
        val setScope = SetScope()
        setScope.scope(inner)
        return inner.createReference("CREATE ${getReference()} ${setScope.getSetString()} RETURN AFTER") as U
    }

    inline fun <reified T, U: RecordType<T>> Table<T, U>.insert(vararg items: T): ListType<T, U> {
        return insert(items.toList())
    }

    inline fun <reified T, U: RecordType<T>> Table<T, U>.insert(items: List<T>): ListType<T, U> {
        return ListType(recordType, "INSERT INTO $name ${surrealJson.encodeToString(items)}")
    }

    inline fun <a, A: RecordType<a>, reified b, B: RelationType<a, A, b, c, C>, c, C: RecordType<c>>relate(from: ListType<a, A>, with: Table<b, B>, to: ListType<c, C>, content: b): ListType<b, B> {
        return ListType(with.recordType, "RELATE ${from.getReference()}->${with.name}->${to.getReference()} CONTENT ${surrealJson.encodeToString(content)}")
    }

    inline fun <a, A: RecordType<a>, reified b, B: RelationType<a, A, b, c, C>, c, C: RecordType<c>>relate(from: A, with: Table<b, B>, to: ListType<c, C>, content: b): ListType<b, B> {
        return ListType(with.recordType, "RELATE ${from.getReference()}->${with.name}->${to.getReference()} CONTENT ${surrealJson.encodeToString(content)}")
    }

    inline fun <a, A: RecordType<a>, reified b, B: RelationType<a, A, b, c, C>, c, C: RecordType<c>>relate(from: ListType<a, A>, with: Table<b, B>, to: C, content: b): ListType<b, B> {
        return ListType(with.recordType, "RELATE ${from.getReference()}->${with.name}->${to.getReference()} CONTENT ${surrealJson.encodeToString(content)}")
    }

    inline fun <a, A: RecordType<a>, reified b, B: RelationType<a, A, b, c, C>, c, C: RecordType<c>>relate(from: A, with: Table<b, B>, to: C, content: b): ListType<b, B> {
        return ListType(with.recordType, "RELATE ${from.getReference()}->${with.name}->${to.getReference()} CONTENT ${surrealJson.encodeToString(content)}")
    }

    inline fun <reified T, U: RecordType<T>> TableId<T, U>.createContent(content: T): U{
        return inner.createReference("CREATE ${getReference()} CONTENT ${surrealJson.encodeToString(content)}") as U
    }

    inline fun <reified T, U: RecordType<T>, a, A: Reference<a>> TableId<T, U>.createContent(content: T, toReturn: U.() -> A): A {
        val returned = toReturn(inner)
        return returned.createReference("CREATE ${getReference()} CONTENT ${surrealJson.encodeToString(content)} RETURN VALUE ${returned.getReference()}") as A
    }

    fun <T, U: RecordType<T>>Table<T, U>.delete(): ListType<String?, NullableType<String, StringType>> {
        return ListType(NullableType("_", stringType), "(DELETE FROM $name)")
    }

    fun <a, A: Reference<a>, T, U: RecordType<T>>Table<T, U>.delete(deleteScope: ReturningFilterScope<T, U>.(U) -> A): ListType<String?, NullableType<String, StringType>> {
        val filter = ReturningFilterScopeImpl(recordType)
        val returned = with(filter) { deleteScope(recordType) }
        return ListType(NullableType("_", stringType), "(DELETE FROM $name${filter.getFilterString()} RETURN ${returned.getReference()})")
    }

    fun <T, U: RecordType<T>>Table<T, U>.selectAll(selectScope: FilterScope.(U) -> Unit = {}): ListType<T, U> {
        val filter = FilterScopeImpl(recordType)
        with(filter) { selectScope(recordType) }
        return ListType(recordType, "(SELECT * FROM $name${filter.getFilterString()})")
    }

    fun <a, A: Reference<a>, T, U: RecordType<T>, r, R: Reference<r>>Table<T, U>.select(projection: FilterScope.(U) -> R): ListType<r, R> {
        val filter = FilterScopeImpl(recordType)
        val toSelect = with(filter) {
            projection(recordType)
        }
        return ListType(
            toSelect,
            "(SELECT ${if (toSelect is RowType) "" else "VALUE " }${toSelect.getReference()} FROM $name${filter.getFilterString()})"
        )
    }

    fun <a, A: Reference<a>, T, U: RecordType<T>, r, R: Reference<r>>Table<T, U>.update(updateScope: SettableFilterScopeImpl<T, U>.(U) -> A): ListType<a, A> {
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

    operator fun <T, U: RecordType<T>>Table<T, U>.get(id: String): TableId<T, U> {
        return TableId("$name:$id", recordType)
    }
}



