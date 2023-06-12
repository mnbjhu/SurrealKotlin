
@file:Suppress("UNCHECKED_CAST")
package uk.gibby.dsl.core

import uk.gibby.dsl.scopes.CodeBlockScope
import uk.gibby.dsl.scopes.TransactionScope
import uk.gibby.dsl.types.*
import kotlin.time.Duration

abstract class Schema(tables: List<uk.gibby.dsl.core.Table<*, *>>) {

    constructor(vararg tables: uk.gibby.dsl.core.Table<*, *>): this(tables.toList())
    fun init(){
        if(!isInitialized){
            with(SchemaScope()){
                configure()
            }
            isInitialized = true
        }
    }

    private var isInitialized = false
    val tables: List<uk.gibby.dsl.core.TableDefinition> = tables.map { uk.gibby.dsl.core.getTableDefinition(it) }
    open val scopes: List<uk.gibby.dsl.core.Scope<*, *, *, *, *, *>> = listOf()
    fun getDefinitionQuery(): String {
        init()
        var definition = "BEGIN TRANSACTION;\n"
        tables.forEach {
            definition += "${it.getDefinition()}\n"
        }
        scopes.forEach {
            definition += "${it.getDefinition()};\n"
        }
        return "$definition\nCOMMIT TRANSACTION;"
    }
    open fun uk.gibby.dsl.core.Schema.SchemaScope.configure() {}

    inner class SchemaScope {

        fun <T, U: RecordType<T>> uk.gibby.dsl.core.Table<T, U>.configureFields(
            configuration: uk.gibby.dsl.core.Schema.TableDefinitionScope.(U) -> Unit
        ) {
            configuration(TableDefinitionScope(this), recordType)
        }
        fun <T, U: RecordType<T>, c, C: RecordType<c>> uk.gibby.dsl.core.Table<T, U>.permissions(`for`: uk.gibby.dsl.core.Scope<*, *, *, *, c, C>, vararg types: uk.gibby.dsl.core.PermissionType, `when`: U.(C) -> BooleanType): uk.gibby.dsl.core.Table<T, U> {
            val definition = tables.first { it.name == name }
            types.forEach {
                val current = definition.permissions.getOrPut(it) { "" }
                val token = recordType.createReference("\$auth") as C
                definition.permissions[it] = current + "IF (\$scope == \"${`for`.name}\") THEN ${recordType.`when`(token).getReference()} ELSE "
            }
            return this
        }

        fun <T, U: RecordType<T>> uk.gibby.dsl.core.Table<T, U>.noPermissionsFor(`for`: uk.gibby.dsl.core.Scope<*, *, *, *, *, *>, vararg types: uk.gibby.dsl.core.PermissionType): uk.gibby.dsl.core.Table<T, U> {
            val definition = tables.first { it.name == name }
            types.forEach {
                val current = definition.permissions.getOrPut(it) { "" }
                definition.permissions[it] = current + "IF (\$scope == \"${`for`.name}\") THEN FALSE ELSE "
            }
            return this
        }

        fun <T, U: RecordType<T>> uk.gibby.dsl.core.Table<T, U>.fullPermissionsFor(`for`: uk.gibby.dsl.core.Scope<*, *, *, *, *, *>, vararg types: uk.gibby.dsl.core.PermissionType): uk.gibby.dsl.core.Table<T, U> {
            val definition = tables.first { it.name == name }
            types.forEach {
                val current = definition.permissions.getOrPut(it) { "" }
                definition.permissions[it] = current + "IF (\$scope == \"${`for`.name}\") THEN TRUE ELSE "
            }
            return this
        }
/*
        fun <T, U: RecordType<T>>Table<T, U>.assert(){
            val definition = tables.first { it.name == name }
            definition
        }
 */
    }

    inner class TableDefinitionScope(private val definition: uk.gibby.dsl.core.TableDefinition) {
        constructor(table: uk.gibby.dsl.core.Table<*, *>): this(tables.first { it.name == table.name })
        fun <T, U: Reference<T>>U.assert(condition: (U) -> BooleanType): U {
            val assertion = condition(createReference("\$value") as U).getReference()
            definition.fields[getReference()]!!.assertions.add(assertion)
            return this
        }

        fun defineIndex(name: String, vararg fields: Reference<*>) {
            definition.indexes.add("DEFINE INDEX $name ON ${definition.name} FIELDS ${fields.joinToString { it.getReference() }};")
        }
        fun defineUniqueIndex(name: String, vararg fields: Reference<*>) {
            definition.indexes.add("DEFINE INDEX $name ON ${definition.name} FIELDS ${fields.joinToString { it.getReference() }} UNIQUE;")
        }

        fun <T, U: Reference<T>, c, C: RecordType<c>>U.permissions(`for`: uk.gibby.dsl.core.Scope<*, *, *, *, c, C>, vararg types: uk.gibby.dsl.core.PermissionType, `when`: (C) -> BooleanType){
            val fieldDefinition = definition.fields[getReference()]!!
            types.forEach {
                val current = fieldDefinition.permissions.getOrPut(it) { "" }
                val token = `for`.tokenTable.recordType.createReference("\$auth") as C
                fieldDefinition.permissions[it] = current + "IF (\$scope == \"${`for`.name}\") THEN ${`when`(token).getReference()} ELSE "
            }
        }
    }

}

fun getTableDefinition(table: uk.gibby.dsl.core.Table<*, *>): uk.gibby.dsl.core.TableDefinition {
    val fieldDefinitions = table.recordType.getFields().toList().flatMap { entry ->  uk.gibby.dsl.core.getFieldDefinition(
        entry.first,
        entry.second
    ).map { entry.first to it  } }.toMap().toMutableMap()
    return uk.gibby.dsl.core.TableDefinition(table.name, fieldDefinitions, mutableMapOf(), mutableListOf())
}

fun getFieldDefinition(name: String, type: Reference<*>): List<uk.gibby.dsl.core.FieldDefinition> {
    return when(type) {
        is StringType -> listOf(
            uk.gibby.dsl.core.FieldDefinition(
                uk.gibby.dsl.core.FieldDefinition.Type.STRING,
                mutableMapOf(),
                mutableListOf(uk.gibby.dsl.core.assertNotNull)
            )
        )
        is BooleanType -> listOf(
            uk.gibby.dsl.core.FieldDefinition(
                uk.gibby.dsl.core.FieldDefinition.Type.BOOLEAN,
                mutableMapOf(),
                mutableListOf(uk.gibby.dsl.core.assertNotNull)
            )
        )
        is LongType -> listOf(
            uk.gibby.dsl.core.FieldDefinition(
                uk.gibby.dsl.core.FieldDefinition.Type.LONG,
                mutableMapOf(),
                mutableListOf(uk.gibby.dsl.core.assertNotNull)
            )
        )
        is DoubleType -> listOf(
            uk.gibby.dsl.core.FieldDefinition(
                uk.gibby.dsl.core.FieldDefinition.Type.DOUBLE,
                mutableMapOf(),
                mutableListOf(uk.gibby.dsl.core.assertNotNull)
            )
        )
        is DurationType -> listOf(
            uk.gibby.dsl.core.FieldDefinition(
                uk.gibby.dsl.core.FieldDefinition.Type.DURATION,
                mutableMapOf(),
                mutableListOf(uk.gibby.dsl.core.assertNotNull)
            )
        )
        is DateTimeType -> listOf(
            uk.gibby.dsl.core.FieldDefinition(
                uk.gibby.dsl.core.FieldDefinition.Type.DATETIME,
                mutableMapOf(),
                mutableListOf(uk.gibby.dsl.core.assertNotNull)
            )
        )
        is RecordLink<*, *> -> listOf(
            uk.gibby.dsl.core.FieldDefinition(
                uk.gibby.dsl.core.FieldDefinition.Type.RecordLink(
                    type.inner.getReference()
                ), mutableMapOf(), mutableListOf(uk.gibby.dsl.core.assertNotNull)
            )
        )
        is ObjectType<*> -> listOf(
            uk.gibby.dsl.core.FieldDefinition(
                uk.gibby.dsl.core.FieldDefinition.Type.OBJECT,
                mutableMapOf(),
                mutableListOf(uk.gibby.dsl.core.assertNotNull)
            )
        ) + type.getFields().flatMap { uk.gibby.dsl.core.getFieldDefinition("$name." + it.key, it.value) }
        is ListType<*, *> -> listOf(
            uk.gibby.dsl.core.FieldDefinition(
                uk.gibby.dsl.core.FieldDefinition.Type.ARRAY,
                mutableMapOf(),
                mutableListOf(uk.gibby.dsl.core.assertNotNull)
            )
        ) + uk.gibby.dsl.core.getFieldDefinition("$name.*", type.inner)
        else -> throw Exception("Unknown type ${type::class}")
    }
}



const val assertNotNull = "\$value != NONE"

class TableDefinition(
    val name: String,
    val fields: MutableMap<String, uk.gibby.dsl.core.FieldDefinition>,
    val permissions: MutableMap<uk.gibby.dsl.core.PermissionType, String> = mutableMapOf(),
    val indexes: MutableList<String> = mutableListOf()
) {
    fun getDefinition(): String {
        return "DEFINE TABLE $name SCHEMAFULL" +
                ( if(permissions.isNotEmpty())
                    "\nPERMISSIONS \n${permissions.entries.joinToString("\n"){ "FOR ${it.key.text} WHERE ${it.value}FALSE END" }}" else "" ) +
                ";\n" +
                fields.entries.joinToString("\n"){ it.value.getDefinition(it.key, name) } + "\n" +
                indexes.joinToString("\n")
    }
}

data class FieldDefinition(
    val type: uk.gibby.dsl.core.FieldDefinition.Type,
    val permissions: MutableMap<uk.gibby.dsl.core.PermissionType, String>,
    val assertions: MutableList<String>
) {
    sealed class Type(val text: String) {
        object BOOLEAN: uk.gibby.dsl.core.FieldDefinition.Type("bool")
        object STRING: uk.gibby.dsl.core.FieldDefinition.Type("string")
        object LONG: uk.gibby.dsl.core.FieldDefinition.Type("int")
        object DOUBLE: uk.gibby.dsl.core.FieldDefinition.Type("decimal")
        object DATETIME: uk.gibby.dsl.core.FieldDefinition.Type("datetime")
        object DURATION: uk.gibby.dsl.core.FieldDefinition.Type("duration")
        object ARRAY: uk.gibby.dsl.core.FieldDefinition.Type("array")
        object OBJECT: uk.gibby.dsl.core.FieldDefinition.Type("object")
        class RecordLink(tableName: String): uk.gibby.dsl.core.FieldDefinition.Type("record($tableName)")

    }
    fun getDefinition(name: String, tableName: String): String {
        return "DEFINE FIELD $name ON TABLE $tableName TYPE ${type.text}" +
                ( if(assertions.isNotEmpty()) "\nASSERT ${assertions.joinToString(" AND "){ it }}" else "" ) +
                ( if(permissions.isNotEmpty())
                    "\nPERMISSIONS \n${permissions.entries.joinToString("\n"){ "FOR ${it.key.text} WHERE ${it.value}FALSE END" }}" else "" ) +
                ";"

    }
}


fun <a, A: Reference<a>, b, B: Reference<b>, c, C: RecordType<c>>scopeOf(
    name: String,
    sessionDuration: Duration,
    signupType: A,
    signInType: B,
    tokenTable: uk.gibby.dsl.core.Table<c, C>,
    signUp: TransactionScope.(A) -> C,
    signIn: TransactionScope.(B) -> ListType<c, C>,
) = object: uk.gibby.dsl.core.Scope<a, A, b, B, c, C>(
    name,
    sessionDuration,
    signupType,
    signInType,
    tokenTable,

    ) {
    override fun signUp(auth: A): ListType<c, C> {
        return signUp(TransactionScope(), auth).run {
            list(this).createReference(getReference().removePrefix("RETURN "))
        }
    }

    override fun signIn(auth: B): ListType<c, C> {
        return signIn(TransactionScope(), auth)
    }

}

abstract class Scope<a, A: Reference<a>, b, B: Reference<b>, c, C: RecordType<c>>(
    val name: String,
    private val sessionDuration: Duration,
    private val signupType: A,
    private val signInType: B,
    internal val tokenTable: uk.gibby.dsl.core.Table<c, C>
) {
    abstract fun signUp(auth: A): ListType<c, C>
    abstract fun signIn(auth: B): ListType<c, C>

    fun getDefinition(): String {
        val signUpToken = signUp(signupType.createReference("\$creds") as A)

        val signInToken = signIn(signInType.createReference("\$creds") as B)

        return "DEFINE SCOPE $name\n" +
                "SESSION $sessionDuration\n" +
                "SIGNUP ( ${signUpToken.getReference()} )\n" +
                "SIGNIN ( ${signInToken.getReference()} )\n"
    }
    inner class Permission(
        val type: uk.gibby.dsl.core.PermissionType,
        val forScope: uk.gibby.dsl.core.Scope<*, *, *, *, *, *>,
        val condition: CodeBlockScope.(C) -> BooleanType
    )
}
enum class PermissionType(val text: String) {
    Create("create"), Update("update"), Select("select"), Delete("delete")
}
