package uk.gibby.example.e2e.statements

import Genre
import genre
import uk.gibby.example.e2e.DatabaseTest
import kotlinx.coroutines.test.runTest
import uk.gibby.dsl.core.transaction
import kotlin.test.Test
import kotlin.test.assertEquals

class Insert: DatabaseTest(){

    @Test
    fun basicInsert() {
        insertInto()
    }

    fun insertInto() {
        runTest {
            val genres = listOf(Genre("Action"), Genre("Horror"), Genre("Comedy"), Genre("Sci-Fi"))
            assertEquals(genres, db.transaction { genre.insert(genres) } )
        }
    }
}