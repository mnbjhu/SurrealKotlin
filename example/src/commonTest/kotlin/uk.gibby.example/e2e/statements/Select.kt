package uk.gibby.example.e2e.statements

import actedIn
import uk.gibby.example.e2e.DatabaseTest
import uk.gibby.example.e2e.statements.Relate.Companion.`RELATE $from - $with - $to`
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import movie
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should contain same`
import org.amshove.kluent.`should match`
import org.junit.jupiter.api.Test
import person
import schema.*
import uk.gibby.dsl.core.transaction
import uk.gibby.dsl.model.Linked
import uk.gibby.dsl.types.*
import uk.gibby.example.schema.Directed
import uk.gibby.example.schema.Person
import kotlin.test.Test

class Select: DatabaseTest() {

    @Test
    fun testSelectAllFromTable() {
        selectAllFromTable()
    }

    @Test
    fun testSelectProjectionFromTableId() {
        `SELECT $projection FROM $table`()
    }

    @Test
    fun testSelectRelatedTableIds() {
        `SELECT $from - $with - $to FROM $table`()
    }

    @Test
    fun testSelectRelatedTable() {
        `SELECT $from - $with - $to all FROM $table`()
    }

    @Test
    fun testSelectRelationId() {
        `SELECT $from - $with FROM $table`()
    }

    @Test
    fun testSelectPath() {
        `SELECT $path FROM $table`()
    }

    suspend fun selectAllFromTable(){
        `RELATE $from - $with - $to`(db)
        runBlocking {
            db.transaction {
                movie.selectAll()
            }
        }.also { it.size `should be equal to` 1 }.first()
            .apply {
                title `should be equal to` "Pulp Fiction"
                genres.size `should be equal to` 3
                released `should be equal to` Instant.parse("1994-10-21T00:00:00Z")
                rating `should be equal to` 8.9
            }
    }
    suspend fun selectProjectionFromTable(){
        `RELATE $from - $with - $to`(db)
            db.transaction {
                movie.select { title }
            }
         `should contain same` listOf("Pulp Fiction")
    }

    suspend fun selectRelatedFromTable(){
        Relate.relate(db)
        db.transaction {
            person.select {
                where(it.name eq "John Travolta")
                it.arrowTo(actedIn).arrowTo(movie)
            }
        }.first().first().apply {
            `should be instance of`<Linked.Reference<*>>()
            id `should match` "^Movie:.*".toRegex()
        }
    }

    suspend fun selectAllRelatedFromTable(){
        `RELATE $from - $with - $to`(db)
        runBlocking {
            db.transaction {
                person.select {
                    where(name eq "John Travolta")
                    `o-→`(actedIn).`o-→`(movie).STAR
                }
            }.also { it.size `should be equal to` 1 }
                .first().first()
                .apply {
                    title `should be equal to` "Pulp Fiction"
                    genres.size `should be equal to` 3
                    released `should be equal to` Instant.parse("1994-10-21T00:00:00Z")
                    rating `should be equal to` 8.9
                }
        }
    }

    suspend fun selectRelationFromTable(){
        `RELATE $from - $with - $to`(db)
        runBlocking {
            db.transaction {
                person.select {
                    where(name eq "John Travolta")
                    `o-→`(actedIn)
                }
            }.also { it.size `should be equal to` 1 }.first().first().apply {
                `should be instance of`<Linked.Reference<*>>()
                this as Linked.Reference<*>
                id `should match` "^ActedIn:.*$".toRegex()
            }
        }
    }

    suspend fun selectPathFromTable(){
        `RELATE $from - $with - $to`(db)
        runBlocking {
            db.transaction {
                val quentin by person.createContent(
                    Person(
                        name = "Quentin Tarantino",
                        dateOfBirth = Instant.parse("1963-03-27T00:00:00Z")
                    )
                )
                val pulpFiction by movie.selectAll { where( title eq "Pulp Fiction") }
                +relate(quentin, directed, pulpFiction, Directed())
                person.select {
                    where(name eq "John Travolta")
                    `o-→`(actedIn).`o-→`(movie).`←-o`(directed).`←-o`(person).STAR
                }
            }.also { println(it) }
        }
    }
}