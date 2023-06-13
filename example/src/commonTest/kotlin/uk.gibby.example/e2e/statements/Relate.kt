package uk.gibby.example.e2e.statements

import ActedIn
import actedIn
import uk.gibby.example.e2e.DatabaseTest
import kotlinx.coroutines.test.runTest
import movie
import person
import uk.gibby.driver.DatabaseConnection
import uk.gibby.dsl.core.transaction
import uk.gibby.dsl.types.eq
import kotlin.test.Test
import kotlin.test.assertEquals

open class Relate: DatabaseTest() {

    @Test
    fun basicRelateTest() = runTest {
        relate(db)
    }

    companion object {

        suspend fun relate(db: DatabaseConnection){
            Create.createTableWithBuilder(db)
            val result = db.transaction {
                val johnTravolta by person.selectAll { where(it.name eq "John Travolta") }
                val samLJackson by person.selectAll { where(it.name eq "Samuel L. Jackson") }
                val pulpFiction by movie.selectAll { where(it.title eq "Pulp Fiction") }
                +relate(johnTravolta, actedIn, pulpFiction, ActedIn(role = "Vincent Vega"))
                relate(samLJackson, actedIn, pulpFiction, ActedIn(role = "Jules Winfield"))
            }.first()
            assertEquals(ActedIn(role = "Jules Winfield"), result)
        }
    }

}