import core.transaction
import uk.gibby.driver.DatabaseConnection
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import scopes.create
import types.eq
import uk.gibby.driver.rpc.RpcRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class BasicTest {


    @Test
    fun testDatabaseConnection() = runTest {
        val connection = DatabaseConnection("localhost", 8000)
        with(connection) {
            connect()
            signInAsRoot("root", "root")
            use("test", "test")
            val result = transaction {
                +user.create { it.username setAs "testUser"; it.passwordHash setAs "testPassword" }
                user.selectAll {
                    where(it.username eq "testUser")
                    it.username
                }
            }
            println(result)
        }
    }
    @Test
    fun testSerialization() {
        val test = TestClass("test")
        val serialized = Json.encodeToString(TestClass.serializer(), test)
        val deserialized = Json.decodeFromString<TestClass>(serialized)
        assertEquals(test, deserialized)
    }

    @Test
    fun testUserSerialization() {
        val test = User("test", "test")
        val serialized = Json.encodeToString(test)
        val deserialized = Json.decodeFromString<User>(serialized)
        assertEquals(test, deserialized)
    }

    @Test
    fun testRpcRequestSerialization() {
        val test = RpcRequest("test", "test", JsonArray(listOf(Json.encodeToJsonElement("test"))))
        val serialized = Json.encodeToString(test)
        val deserialized = Json.decodeFromString<RpcRequest>(serialized)
        assertEquals(test, deserialized)
    }
}
@Serializable
data class TestClass(val test: String)
