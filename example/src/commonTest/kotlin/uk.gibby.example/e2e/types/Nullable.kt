package uk.gibby.example.e2e.types

import uk.gibby.example.e2e.DatabaseTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import uk.gibby.dsl.core.of
import uk.gibby.dsl.functions.`??`
import uk.gibby.dsl.types.nullable
import uk.gibby.dsl.types.stringType



class NullableTest: DatabaseTest() {
    @Test
    fun `coal test`() {
        runBlocking {
            db.transaction {
                val myNullInstance = nullable(stringType) of null
                myNullInstance `??` "something"
            } `should be equal to` "something"

            db.transaction {
                val myString = nullable(stringType) of "other thing"
                myString `??` "something"
            } `should be equal to` "other thing"
        }
    }

    @Test
    fun tempTest() {
        runBlocking {
            delay(100000)
        }
    }
}




@Testcontainers



class ContainerTest {
    @Test
    fun myTest(){
        runBlocking {
            println()
            delay(1000000)
        }
    }
    companion object {
        @Container
        var surrealDb: GenericContainer<*> = GenericContainer<Nothing>(DockerImageName.parse("surrealdb/surrealdb:latest")).apply {
            withExposedPorts(8000)
            withCommand("start -u root -p root --strict")
            this.start()
        }
    }
}