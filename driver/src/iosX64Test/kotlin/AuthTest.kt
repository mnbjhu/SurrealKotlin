import driver.DatabaseConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class AuthTest {

    private val db = DatabaseConnection("localhost")
    @Test
    fun testLoginAsRoot() {
        println("running")
        runBlocking {
            db.connect()
            db.signInAsRoot("root", "root")
            db.invalidate()
        }
    }
}