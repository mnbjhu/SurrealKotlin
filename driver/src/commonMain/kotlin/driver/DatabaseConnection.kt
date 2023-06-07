package driver

import driver.rpc.RootAuth
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.util.collections.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.*
import driver.rpc.RpcRequest
import driver.rpc.RpcResponse
import driver.rpc.RpcResponseSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable


@Serializable
data class LiveQueryNotification(val id: String, val method: String, val result: JsonElement)

class DatabaseConnection(private val host: String, private val port: Int = 8000) {
    val scope = CoroutineScope(Dispatchers.Default)
    private var count = 0L
    private var connection: DefaultClientWebSocketSession? = null
    private val requests = ConcurrentMap<String, Channel<JsonElement>>()
    private val liveQueries = ConcurrentMap<String, Channel<LiveQueryNotification>>()
    private val context = CoroutineScope(Dispatchers.Default)

    suspend fun connect() {
        connection?.cancel()
        connection = Client.webSocketSession(method = HttpMethod.Get, host = host, port = port, path = "/rpc").also {
            context.launch {
                it.incoming.receiveAsFlow().collect {
                    it as Frame.Text
                    val response = try {
                        surrealJson.decodeFromString(RpcResponseSerializer, it.readText())
                    } catch (e: Exception) {
                        requests.forEach { (_, r) ->  r.cancel(CancellationException("Failed to decode incoming response: ${it.readText()}\n${e.message}"))}
                        throw e
                    }
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

    suspend fun liveSelect(table: String, where: String? = null): Flow<List<JsonElement>> {
        val liveQueryId = surrealJson.decodeFromJsonElement<String>(query("LIVE SELECT * FROM $table;"))
        val channel = Channel<LiveQueryNotification>()
        liveQueries[liveQueryId] = channel
        val state = (query("SELECT * FROM $table;") as JsonArray).toMutableList()
        return flow {
            emit(state)
            channel.receive()
            for (notification in channel) {
                when (notification.method) {
                    "create" -> {
                        state.add(notification.result)
                        emit(state)
                    }
                    "update" -> {
                        val index = state.indexOfFirst { it.jsonObject["id"] == notification.result.jsonObject["id"] }
                        state[index] = notification.result
                        emit(state)
                    }
                    "delete" -> {
                        val index = state.indexOfFirst { it.jsonObject["id"] == notification.result.jsonObject["id"] }
                        state.removeAt(index)
                        emit(state)
                    }
                }
            }
        }
    }

    suspend fun invalidate(){
        sendRequest("invalidate", JsonArray(listOf()))
    }
}