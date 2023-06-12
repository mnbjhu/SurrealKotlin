package uk.gibby.example.e2e

import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.Test
import schema.*
import uk.gibby.example.schema.SurrealTvSchema
import uk.gibby.example.schema.UserSignUpDetails
import uk.gibby.example.schema.userScope

class SignUpTest: DatabaseTest() {

    override suspend fun setupDatabase() {
        super.setupDatabase()
        db.use(namespaceName, databaseName)
        db.define(SurrealTvSchema)
        db.invalidate()
        db.use(namespaceName, databaseName)
    }

    @Test
    fun signUpTest() {
        runBlocking {
            db.signUp(
                namespaceName, databaseName, userScope, UserSignUpDetails(
                testUserCredentials,
                testUserDetails
            )
            )
            val userData = db.transaction { user.selectAll() }.also { it.size `should be equal to` 1 }.first()
            userData.details `should be equal to` testUserDetails
            userData.username `should be equal to` testUserCredentials.username
            userData.passwordHash `should not be equal to` testUserCredentials.password
            userData.isAdmin `should be equal to` false
        }
    }

    @Test
    fun signInTest() {
        runBlocking {
            signUpTest()
            db.invalidate()
            db.signIn(namespaceName, databaseName, userScope, testUserCredentials)
            db.transaction { user.selectAll() }
                .also { it.size `should be equal to` 1 }.first()
                .apply {
                    details `should be equal to` testUserDetails
                    username `should be equal to` testUserCredentials.username
                    passwordHash `should not be equal to` testUserCredentials.password
                    isAdmin `should be equal to` false
                }
        }
    }

}