package uk.gibby.example.e2e

import UserCredentials
import UserDetails
import kotlinx.coroutines.test.runTest
import uk.gibby.example.e2e.types.ContainerTest
import kotlinx.datetime.Instant
import uk.gibby.driver.DatabaseConnection
import uk.gibby.example.schema.SurrealTvSchema
import kotlin.test.BeforeTest

abstract class DatabaseTest {

    protected val testUserCredentials = UserCredentials("mnbjhu", "testpass")
    protected val testUserDetails = UserDetails("James", "Gibson", Instant.parse("1999-03-31T00:00:00Z"), "james.gibson@test.com", "441234567890")
    protected val databaseName = this@DatabaseTest::class.simpleName.toString()
    protected val db = DatabaseConnection("localhost", port = ContainerTest.surrealDb.getMappedPort(8000))
    open suspend fun setupDatabase() {
        runTest {
            db.connect()
            db.signInAsRoot("root", "root")
            db.removeDatabase(namespaceName, databaseName)
            db.defineDatabase(namespaceName, databaseName)
            db.use(namespaceName, databaseName)
            db.define(SurrealTvSchema)
        }
    }

    @BeforeTest
    fun setup(){
        runTest { setupDatabase() }
    }


    companion object {
        const val namespaceName = "e2e_tests"
    }
}

