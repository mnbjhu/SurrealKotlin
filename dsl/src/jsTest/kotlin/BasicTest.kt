import core.transaction
import driver.DatabaseConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import scopes.create
import types.eq
import kotlin.test.Test
import kotlin.test.assertEquals

class BasicTest {


    @Test
    fun testDatabaseConnection() {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
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
                assertEquals(result.first(), "testUser")
            }
        }
    }
    @Test
    fun test(){

    }
}
