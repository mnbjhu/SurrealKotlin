import core.transaction
import driver.DatabaseConnection
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import scopes.None
import scopes.insert
import kotlin.system.measureTimeMillis



class PerformanceTest {
    @Test
    fun testBulkInsertPerformance() = runTest {
        val db = DatabaseConnection("localhost").apply {
            connect()
            use("test", "performance")
            signInAsRoot("root", "root")
        }
        val users = (0..100000).map { UserTable(it.toLong(), "user$it", "password$it") }
        runBlocking {
            userTable.delete()
            measureTimeMillis {
                db.transaction {
                    userTable.insert(users)
                    None
                }
                Unit
            }.also { println(it) }
        }
    }


    @Test
    fun testInsertPerformance() {
        runBlocking {
            val db = DatabaseConnection("localhost").apply {
                connect()
                use("test", "performance")
                signInAsRoot("root", "root")
            }
            runBlocking {
                userTable.delete()
                measureTimeMillis {
                    for(i in 0..100000) {
                        db.transaction {
                            userTable.insert(UserTable(i.toLong(), "user$i", "password$i"))
                            None
                        }
                    }
                }.also { println(it) }
            }
        }
    }



    @Test
    fun testInsertConcurrentPerformance() {
        runBlocking {
            val db = DatabaseConnection("localhost").apply {
                connect()
                use("test", "performance")
                signInAsRoot("root", "root")
            }
            userTable.delete()

            measureTimeMillis {
                runBlocking {
                    for(i in 0..100000) {
                        launch {
                            db.transaction {
                                userTable.insert(UserTable(i.toLong(), "user$i", "password$i"))
                                None
                            }
                            Unit
                        }
                    }
                }
            }.also { println(it) }
        }
    }
}