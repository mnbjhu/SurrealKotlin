package core

import scopes.CodeBlockScope
import scopes.TransactionScope
import scopes.TransactionScopeImpl
import types.*
import kotlin.time.Duration

abstract class Schema(tables: List<Table<*, *>>) {

    constructor(vararg tables: Table<*, *>): this(tables.toList())
    fun init(){
        if(!isInitialized){
            with(SchemaScope()){
                configure()
            }
            isInitialized = true
        }
    }

    private var isInitialized = false
    val tables: List<TableDefinition> = tables.map { getTableDefinition(it) }
    open val scopes: List<Scope<*, *, *, *, *, *>> = listOf()
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
    open fun SchemaScope.configure() {}

    inner class SchemaScope {

        fun <T, U: RecordType<T>> Table<T, U>.configureFields(
            configuration: TableDefinitionScope.(U) -> Unit
        ) {
            configuration(TableDefinitionScope(this), recordType)
        }
        fun <T, U: RecordType<T>, c, C: RecordType<c>> Table<T, U>.permissions(`for`: Scope<*, *, *, *, c, C>, vararg types: PermissionType, `when`: U.(C) -> BooleanType): Table<T, U> {
            val definition = tables.first { it.name == name }
            types.forEach {
                val current = definition.permissions.getOrPut(it) { "" }
                val token = recordType.createReference("\$auth") as C
                definition.permissions[it] = current + "IF (\$scope == \"${`for`.name}\") THEN ${recordType.`when`(token).getReference()} ELSE "
            }
            return this
        }

        fun <T, U: RecordType<T>> Table<T, U>.noPermissionsFor(`for`: Scope<*, *, *, *, *, *>, vararg types: PermissionType): Table<T, U> {
            val definition = tables.first { it.name == name }
            types.forEach {
                val current = definition.permissions.getOrPut(it) { "" }
                definition.permissions[it] = current + "IF (\$scope == \"${`for`.name}\") THEN FALSE ELSE "
            }
            return this
        }

        fun <T, U: RecordType<T>> Table<T, U>.fullPermissionsFor(`for`: Scope<*, *, *, *, *, *>, vararg types: PermissionType): Table<T, U> {
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

    inner class TableDefinitionScope(private val definition: TableDefinition) {
        constructor(table: Table<*, *>): this(tables.first { it.name == table.name })
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

        fun <T, U: Reference<T>, c, C: RecordType<c>>U.permissions(`for`: Scope<*, *, *, *, c, C>, vararg types: PermissionType, `when`: (C) -> BooleanType){
            val fieldDefinition = definition.fields[getReference()]!!
            types.forEach {
                val current = fieldDefinition.permissions.getOrPut(it) { "" }
                val token = `for`.tokenTable.recordType.createReference("\$auth") as C
                fieldDefinition.permissions[it] = current + "IF (\$scope == \"${`for`.name}\") THEN ${`when`(token).getReference()} ELSE "
            }
        }
    }

}

fun getTableDefinition(table: Table<*, *>): TableDefinition {
    val fieldDefinitions = table.recordType.getFields().toList().flatMap { entry ->  getFieldDefinition(entry.first, entry.second).map { entry.first to it  } }.toMap().toMutableMap()
    return TableDefinition(table.name, fieldDefinitions, mutableMapOf(), mutableListOf())
}

fun getFieldDefinition(name: String, type: Reference<*>): List<FieldDefinition> {
    return when(type) {
        is StringType -> listOf(FieldDefinition(FieldDefinition.Type.STRING, mutableMapOf(), mutableListOf(assertNotNull)))
        is BooleanType -> listOf(FieldDefinition(FieldDefinition.Type.BOOLEAN, mutableMapOf(), mutableListOf(assertNotNull)))
        is LongType -> listOf(FieldDefinition(FieldDefinition.Type.LONG, mutableMapOf(), mutableListOf(assertNotNull)))
        is DoubleType -> listOf(FieldDefinition(FieldDefinition.Type.DOUBLE, mutableMapOf(), mutableListOf(assertNotNull)))
        is DurationType -> listOf(FieldDefinition(FieldDefinition.Type.DURATION, mutableMapOf(), mutableListOf(assertNotNull)))
        is DateTimeType -> listOf(FieldDefinition(FieldDefinition.Type.DATETIME, mutableMapOf(), mutableListOf(assertNotNull)))
        is RecordLink<*, *> -> listOf(FieldDefinition(FieldDefinition.Type.RecordLink(type.inner.getReference()), mutableMapOf(), mutableListOf(assertNotNull)))
        is ObjectType<*> -> listOf(FieldDefinition(FieldDefinition.Type.OBJECT, mutableMapOf(), mutableListOf(assertNotNull))) + type.getFields().flatMap { getFieldDefinition("$name." + it.key, it.value) }
        is ListType<*, *> -> listOf(FieldDefinition(FieldDefinition.Type.ARRAY, mutableMapOf(), mutableListOf(assertNotNull))) + getFieldDefinition("$name.*", type.inner)
        else -> throw Exception("Unknown type ${type::class}")
    }
}



const val assertNotNull = "\$value != NONE"

class TableDefinition(
    val name: String,
    val fields: MutableMap<String, FieldDefinition>,
    val permissions: MutableMap<PermissionType, String> = mutableMapOf(),
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
    val type: Type,
    val permissions: MutableMap<PermissionType, String>,
    val assertions: MutableList<String>
) {
    sealed class Type(val text: String) {
        object BOOLEAN: Type("bool")
        object STRING: Type("string")
        object LONG: Type("int")
        object DOUBLE: Type("decimal")
        object DATETIME: Type("datetime")
        object DURATION: Type("duration")
        object ARRAY: Type("array")
        object OBJECT: Type("object")
        class RecordLink(tableName: String): Type("record($tableName)")

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
    tokenTable: Table<c, C>,
    signUp: TransactionScope.(A) -> C,
    signIn: TransactionScope.(B) -> ListType<c, C>,
) = object: Scope<a, A, b, B, c, C>(
    name,
    sessionDuration,
    signupType,
    signInType,
    tokenTable,

    ) {
    override fun signUp(auth: A): ListType<c, C> {
        return signUp(TransactionScopeImpl(), auth).run {
            list(this).createReference(getReference().removePrefix("RETURN "))
        }
    }

    override fun signIn(auth: B): ListType<c, C> {
        return signIn(TransactionScopeImpl(), auth)
    }

}

abstract class Scope<a, A: Reference<a>, b, B: Reference<b>, c, C: RecordType<c>>(
    val name: String,
    private val sessionDuration: Duration,
    private val signupType: A,
    private val signInType: B,
    internal val tokenTable: Table<c, C>
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
        val type: PermissionType,
        val forScope: Scope<*, *, *, *, *, *>,
        val condition: CodeBlockScope.(C) -> BooleanType
    )
}
enum class PermissionType(val text: String) {
    Create("create"), Update("update"), Select("select"), Delete("delete")
}
