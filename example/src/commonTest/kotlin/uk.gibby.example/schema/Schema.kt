package uk.gibby.example.schema

import actedIn
import directed
import genre
import movie
import person
import uk.gibby.dsl.core.PermissionType.*
import uk.gibby.dsl.core.Schema
import uk.gibby.dsl.functions.*
import uk.gibby.dsl.types.eq
import user

object SurrealTvSchema: Schema(person, movie, actedIn, directed, genre, user) {
    override val scopes = listOf(userScope, adminScope)
    override fun SchemaScope.configure() {
        movie.noPermissionsFor(userScope, Create, Update, Delete)
        actedIn.noPermissionsFor(userScope, Create, Update, Delete)
        directed.noPermissionsFor(userScope, Create, Update, Delete)
        genre.noPermissionsFor(userScope, Create, Update, Delete)
        person.noPermissionsFor(userScope, Create, Update, Delete)
        user
            .noPermissionsFor(userScope,  Delete, Create)
            .permissions(userScope, Select, Update) {
                it.id eq id
            }
            .noPermissionsFor(adminScope, Create)
            .configureFields {
                it.details.email.assert { it.isEmail() }
                it.details.dateOfBirth.assert { it lessThan Time.now() }
                it.details.phoneNumber.assert { it.length() eq 12 and it.isNumeric() }
                defineUniqueIndex("usernameIndex", it.username)
                defineUniqueIndex("emailIndex", it.details.email)
            }

    }
}
