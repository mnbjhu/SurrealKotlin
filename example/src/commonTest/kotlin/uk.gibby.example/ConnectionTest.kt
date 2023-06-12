package uk.gibby.example

import kotlinx.coroutines.test.runTest
import uk.gibby.driver.DatabaseConnection
import uk.gibby.example.schema.SurrealTvSchema
import uk.gibby.dsl.core.PermissionType.*
import kotlin.test.Test
import kotlin.test.assertFails

class ConnectionTest {

    private val db = DatabaseConnection("localhost")

    @Test
    fun signInAsRoot() {
        runTest {
            db.connect()
            db.signInAsRoot("root", "root")
        }
    }

    @Test
    fun defineNamespace() {
        runTest {
            db.connect()
            db.signInAsRoot("root", "root")
            db.removeNamespace("test_namespace")
            db.defineNamespace("test_namespace")
        }
    }

    @Test
    fun createDatabase() {
        runTest {
            db.connect()
            db.signInAsRoot("root", "root")
            db.removeNamespace("test_namespace")
            db.defineNamespace("test_namespace")
            db.defineDatabase("test_namespace", "test_database")
            db.use("test_namespace", "test_database")
        }
    }

    @Test
    fun setSchema() {
        runTest {
            db.connect()
            db.signInAsRoot("root", "root")
            db.removeNamespace("test_namespace")
            db.defineNamespace("test_namespace")
            db.defineDatabase("test_namespace", "test_database")
            db.use("test_namespace", "test_database")
            db.define(SurrealTvSchema)
        }
    }

    @Test
    fun logout() {
        runTest {
            db.connect()
            db.signInAsRoot("root", "root")
            db.removeNamespace("test_namespace")
            db.defineNamespace("test_namespace")
            db.invalidate()
            assertFails { db.removeNamespace("test_namespace") }
        }
    }
}