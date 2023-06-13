package uk.gibby.example.e2e.statements

import Genre
import Person
import genre
import uk.gibby.example.e2e.DatabaseTest
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import movie
import person
import uk.gibby.driver.DatabaseConnection
import uk.gibby.dsl.core.transaction
import kotlin.test.Test
import kotlin.test.assertEquals

open class Create: DatabaseTest() {

    @Test
    fun testCreateContent() = runTest {
        createContent(db)
    }

    @Test
    fun testCreateContentWithId() = runTest {
        createTableIdContent(db)
    }

    @Test
    fun testCreateWithReferences() = runTest {
        createTableWithBuilder(db)
    }


    companion object {
        suspend fun createContent(db: DatabaseConnection) {
            db.transaction {
                +genre.createContent(Genre("Action"))
                +genre.createContent(Genre("Thriller"))
                genre.createContent(Genre("Comedy"))
            }

            db.transaction { person.createContent(Person("John Travolta", dateOfBirth = Instant.parse("1954-02-18T00:00:00Z"))) }
            db.transaction { person.createContent(Person("Samuel L. Jackson", dateOfBirth = Instant.parse("1948-12-21T00:00:00Z"))) }
        }

        suspend fun createTableIdContent(db: DatabaseConnection) {
            val result = db.transaction {
                +genre["action"].createContent(Genre("Action"))
                +genre["thriller"].createContent(Genre("Thriller"))
                genre["comedy"].createContent(Genre("Comedy"))
            }
            assertEquals("Comedy", result.name)
        }
        suspend fun createTableWithBuilder(db: DatabaseConnection) {
                createContent(db)
                db.transaction {
                    movie.create {
                        it.title setAs "Pulp Fiction"
                        it.genres setAs genre.select { it.id }
                        it.released setAs Instant.parse("1994-10-21T00:00:00Z")
                        it.rating setAs 8.9
                    }
                }.apply {
                    assertEquals("Pulp Fiction", title)
                    assertEquals(3, genres.size)
                    assertEquals(Instant.parse("1994-10-21T00:00:00Z"), released)
                    assertEquals(8.9, rating)
                }
        }

        suspend fun largeCreate(db: DatabaseConnection) {
                db.transaction {
                    val action by genre.createContent(Genre("Action")) { id }
                    val thriller by genre.createContent(Genre("Thriller")) { id }
                    val comedy by genre.createContent(Genre("Comedy")) { id }
                    movie.create {
                        it.title to "Pulp Fiction"
                        it.genres to listOf(action, thriller, comedy)
                        it.released to Instant.parse("1994-10-21T00:00:00Z")
                        it.rating to 8.9
                    }
                }
        }
    }

}