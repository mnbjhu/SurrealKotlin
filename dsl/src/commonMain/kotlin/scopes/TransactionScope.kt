package scopes

import kotlinx.serialization.encodeToString
import core.Table
import driver.surrealJson
import types.ListType
import types.RecordType
import types.Reference
import types.RelationType
import uk.gibby.dsl.core.TableId
import kotlin.reflect.KProperty


interface TransactionScope {
    operator fun Reference<*>.unaryPlus()
    operator fun <T, U: Reference<T>>U.getValue(thisRef: Any?, property: KProperty<*>): U
}

class TransactionScopeImpl: TransactionScope {
    private var generated: String = "BEGIN TRANSACTION;\n"
    override operator fun Reference<*>.unaryPlus(){
        generated += "RETURN ${getReference()};\n"
    }
    fun getQueryText() = generated + "COMMIT TRANSACTION;"

    override operator fun <T, U: Reference<T>>U.getValue(thisRef: Any?, property: KProperty<*>): U =
        createReference("\$${property.name}")
            .also { generated += "LET \$${property.name} = ${getReference()};\n" }
                as U

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
inline fun <a, A: RecordType<a>, reified b, B: RelationType<a, A, b, c, C>, c, C: RecordType<c>>TransactionScope.relate(from: A, with: Table<b, B>, to: C, content: b): ListType<b, B> {
    return ListType(with.recordType, "RELATE ${from.getReference()}->${with.name}->${to.getReference()} CONTENT ${surrealJson.encodeToString(content)}")
}
inline fun <reified T, U: RecordType<T>> Table<T, U>.createContent(content: T): U =
    recordType.createReference("CREATE $name CONTENT ${surrealJson.encodeToString(content)}") as U
inline fun <reified T, U: RecordType<T>, a, A: Reference<a>> Table<T, U>.createContent(content: T, crossinline toReturn: U.() -> A): A {
    val returned = toReturn(recordType)
    return returned.createReference("CREATE $name CONTENT ${surrealJson.encodeToString(content)} RETURN VALUE ${returned.getReference()}") as A
}
inline fun <reified T, U: RecordType<T>> TableId<T, U>.createContent(content: T): U{
    return inner.createReference("CREATE ${getReference()} CONTENT ${surrealJson.encodeToString(content)}") as U
}
inline fun <reified T, U: RecordType<T>, a, A: Reference<a>> TableId<T, U>.createContent(content: T, toReturn: U.() -> A): A {
    val returned = toReturn(inner)
    return returned.createReference("CREATE ${getReference()} CONTENT ${surrealJson.encodeToString(content)} RETURN VALUE ${returned.getReference()}") as A
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
