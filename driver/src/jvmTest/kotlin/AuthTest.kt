import driver.DatabaseConnection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

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



class LiveQueryTest {

    @Test
    fun basic() {
        runBlocking {
            val db = DatabaseConnection("localhost")
            db.connect()
            db.use("test", "test")
            db.signInAsRoot("root", "root")
            db.query("DELETE test_table;")
            val liveData = db.liveSelect("test_table")
            assertEquals(listOf(), liveData.first())
            db.query("CREATE test_table CONTENT {\"thing\"=\"thing\"};")
            delay(10000)
            assertEquals(listOf(buildJsonObject { put("thing", "thing") }), liveData.first())
        }
    }
}