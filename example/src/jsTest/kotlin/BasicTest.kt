import annotation.Table
import core.getTableDefinition
import core.transaction
import driver.DatabaseConnection
import kotlinx.coroutines.test.runTest
import scopes.create
import types.*
import kotlin.test.Test
import kotlin.test.assertEquals


class BasicTest {

    @Test
    fun testDatabaseConnectionNew() = runTest {
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
            assertEquals(User("testUser", "testPassword"), result.first())
        }
    }

    @Test
    fun schemaGen() {
        println(getTableDefinition(user).getDefinition())
    }
}



