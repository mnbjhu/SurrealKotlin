package scopes

import driver.surrealJson
import kotlinx.serialization.encodeToString
import types.ListType
import types.Reference
import types.nullable
import types.stringType



open class SetScope {
    private var text = "SET "
    fun _addParam(paramText: String) {
        text += paramText
    }

    fun getSetString() = text.removeSuffix(",")


    infix fun <T, U : Reference<T>> U.setAs(value: U): UnitType {
        text += "${getReference()} = (${value.getReference()}),"
        return None
    }

    infix fun <T, U : Reference<T>> ListType<T, U>.setAs(value: List<U>): UnitType {
        text += "${getReference()} = [${value.joinToString { it.getReference() }}],"
        return None
    }

    infix fun <T, U : Reference<T>> ListType<T, U>.`to`(value: List<U>): UnitType {
        text += "${getReference()} = [${value.joinToString { it.getReference() }}],"
        return None
    }


    inline infix fun <reified T> Reference<T>.setAs(value: T): UnitType {
        _addParam("${getReference()} = ${surrealJson.encodeToString(value)},")
        return None
    }

    inline infix fun <reified T> Reference<T>.`to`(value: T): UnitType {
        _addParam("${getReference()} = ${surrealJson.encodeToString(value)},")
        return None
    }
}

val None = nullable(stringType).createReference("NONE")