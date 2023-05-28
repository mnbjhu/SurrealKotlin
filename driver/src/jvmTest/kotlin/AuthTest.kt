import driver.DatabaseConnection
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class AuthTest {

    private val db = DatabaseConnection("localhost")
    @Test
    fun testLoginAsRoot() {
        runBlocking {
            db.connect()
            db.signInAsRoot("root", "root")
            db.invalidate()
        }
    }
}