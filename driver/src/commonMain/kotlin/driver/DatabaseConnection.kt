package driver

import driver.rpc.RootAuth
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.util.collections.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import uk.gibby.dsl.model.rpc.RpcRequest
import driver.rpc.RpcResponse

class DatabaseConnection(private val host: String, private val port: Int = 8000) {

    private var count = 0L
    private var connection: DefaultClientWebSocketSession? = null
    private val requests = ConcurrentMap<String, Channel<JsonElement>>()
    private val context = CoroutineScope(Dispatchers.Default)

    suspend fun connect() {
        connection?.cancel()
        connection = Client.webSocketSession(method = HttpMethod.Get, host = host, port = port, path = "/rpc").also {
            context.launch {
                it.incoming.receiveAsFlow().collect {
                    it as Frame.Text
                    println(it.readText())
                    val response = surrealJson.decodeFromString<RpcResponse>(it.readText())
                    val request = requests[response.id]
                    if (request == null) requests.forEach { (_, r) ->  r.cancel(CancellationException("Received a request with an unknown id: ${response.id} body: $response"))}
                    else when(response) {
                        is RpcResponse.Success -> request.send(response.result)
                        is RpcResponse.Error -> request.cancel(CancellationException("SurrealDB responded with an error.${response.error}"))
                    }
                    requests.remove(response.id)
                }
            }
        }
    }

    suspend fun defineNamespace(name: String) {
        query("DEFINE NAMESPACE $name;")
    }

    suspend fun defineDatabase(ns: String, db: String) {
        query("DEFINE NS $ns; USE NS $ns; DEFINE DATABASE $db;")
    }
    suspend fun removeDatabase(ns: String, db: String) {
        query("USE NS $ns; REMOVE DATABASE $db;")
    }
    suspend fun removeNamespace(name: String) {
        query("REMOVE NAMESPACE $name;")
    }

    private suspend fun sendRequest(method: String, params: JsonArray): JsonElement {
        val id = count++.toString()
        val request = RpcRequest(id, method, params)
        val channel = Channel<JsonElement>(1)
        requests[id] = channel
        (connection ?: throw Exception("SurrealDB: Websocket not connected")).sendSerialized(request)
        return channel.receive()
    }

    suspend fun signInAsRoot(user: String, pass: String): String? {
        val result = sendRequest("signin", surrealJson.encodeToJsonElement(listOf(RootAuth(user, pass))) as JsonArray)
        return surrealJson.decodeFromJsonElement(result)
    }

    suspend fun query(queryText: String): JsonElement {
        println(queryText)
        val result = sendRequest("query", buildJsonArray { add(queryText) })
        return when (val response = surrealJson.decodeFromJsonElement<QueryResponse>((result as JsonArray).last())) {
            is QueryResponse.Error -> throw Exception("surrealDB returned an error: ${response.detail}")
            is QueryResponse.Success -> response.result
        }
    }

    suspend fun use(ns: String, db: String): JsonElement {
        val result = sendRequest("use", buildJsonArray { add(ns); add(db) })
        return surrealJson.decodeFromJsonElement(result)
    }

    suspend fun invalidate(){
        sendRequest("invalidate", JsonArray(listOf()))
    }
}