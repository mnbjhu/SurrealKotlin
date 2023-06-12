package uk.gibby.example.e2e

import kotlinx.coroutines.runBlocking
import uk.gibby.example.schema.SurrealTvSchema
import uk.gibby.example.schema.UserSignUpDetails
import uk.gibby.example.schema.userScope

abstract class UserScopeTest: DatabaseTest(){
    override suspend fun setupDatabase() {
        super.setupDatabase()
        runBlocking {
            db.signUp(
                namespaceName, databaseName, userScope, UserSignUpDetails(
                    testUserCredentials,
                    testUserDetails
                )
            )
            db.use(namespaceName, databaseName)
            db.define(SurrealTvSchema)
            db.invalidate()
            db.use(namespaceName, databaseName)
        }
    }
}