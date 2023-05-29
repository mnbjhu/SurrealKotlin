import driver.DatabaseConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test

class AuthTest {

    private val db = DatabaseConnection("localhost")
    @Test
    fun testLoginAsRoot() {
        println("running")
        CoroutineScope(Dispatchers.Default).coroutineContext.la{
            db.connect()
            db.signInAsRoot("root", "root")
            db.invalidate()
        }
    }
}