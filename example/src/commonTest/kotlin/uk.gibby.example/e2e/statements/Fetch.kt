package uk.gibby.example.e2e.statements

import Genre
import genre
import kotlinx.coroutines.test.runTest
import uk.gibby.example.e2e.DatabaseTest
import kotlinx.datetime.Instant
import movie
import uk.gibby.dsl.core.transaction
import uk.gibby.dsl.model.Linked
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

class Fetch: DatabaseTest() {
    @Test
    fun basicFetch() = runTest {
        Create.createTableWithBuilder(db)
        db.transaction {
            movie.selectAll { fetch(it.genres) }
        }.also {
            assertEquals(1, it.size)
        }.first()
            .apply {
                assertEquals("Pulp Fiction", title)
                assertEquals(Instant.parse("1994-10-21T00:00:00Z"), released)
                assertEquals(8.9, rating)
                assertEquals(3, genres.size)
                val genreNames = genres.map {
                    assertEquals<KClass<*>>(Linked.Actual::class, it::class)
                    it as Linked.Actual<*>
                    assertEquals(Genre::class, it.result!!::class)
                    assertEquals<KClass<*>>(Genre::class, genre::class)
                    (it.result as Genre).name
                }
                assertEquals(listOf("Action", "Thriller", "Comedy"), genreNames)
            }
    }
}