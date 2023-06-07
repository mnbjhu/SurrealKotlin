package core

import driver.DatabaseConnection
import driver.surrealJson
import kotlinx.serialization.json.decodeFromJsonElement
import scopes.TransactionScope
import scopes.TransactionScopeImpl
import types.Reference

suspend inline fun <reified T> DatabaseConnection.transaction(crossinline scope: TransactionScope.() -> Reference<T>): T {
    val transaction = TransactionScopeImpl()
    val result = transaction.scope()
    with(transaction) { +result }
    val queryText = transaction.getQueryText()
    val rawResponse = query(queryText)
    return surrealJson.decodeFromJsonElement(rawResponse)
}
