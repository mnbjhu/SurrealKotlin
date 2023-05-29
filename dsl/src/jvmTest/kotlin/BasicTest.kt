import core.transaction
import driver.DatabaseConnection
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import scopes.create
import types.eq


class BasicTest {

    @Test
    fun testDatabaseConnection() {
        runBlocking {
            val connection = DatabaseConnection("localhost", 8000)
            with(connection){
                connect()
                signInAsRoot("root", "root")
                use("test", "test")
                val result = transaction {
                    +user.create { it.username setAs "testUser"; it.passwordHash setAs "testPassword" }
                    user.select {
                        where(it.username eq "testUser")
                        it.username
                    }
                }
                assert(result.all { it  == "testUser" }) { "Expected testUser, got $result" }
            }
        }
    }
}