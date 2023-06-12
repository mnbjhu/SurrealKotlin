package uk.gibby.dsl.core

import uk.gibby.driver.DatabaseConnection
import kotlinx.serialization.json.decodeFromJsonElement
import uk.gibby.dsl.scopes.TransactionScope
import uk.gibby.dsl.types.Reference

suspend inline fun <reified T> DatabaseConnection.transaction(crossinline scope: TransactionScope.() -> Reference<T>): T {
    val transaction = TransactionScope()
    val result = transaction.scope()
    with(transaction) { +result }
    val queryText = transaction._getQueryText()
    val rawResponse = query(queryText)
    return uk.gibby.driver.surrealJson.decodeFromJsonElement(rawResponse)
}
