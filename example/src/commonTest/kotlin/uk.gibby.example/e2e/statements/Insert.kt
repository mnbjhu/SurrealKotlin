package uk.gibby.example.e2e.statements

import uk.gibby.example.e2e.DatabaseTest
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should contain same`
import org.junit.jupiter.api.Test
import uk.gibby.example.schema.Genre
import schema.genre

class Insert: DatabaseTest(){

    @Test
    fun basicInsert() {
        `INSERT INTO $table $data`()
    }

    fun `INSERT INTO $table $data`() {
        runBlocking {
            val genres = listOf(Genre("Action"), Genre("Horror"), Genre("Comedy"), Genre("Sci-Fi"))
            db.transaction { genre.insert(genres) } `should contain same` genres
        }
    }
}