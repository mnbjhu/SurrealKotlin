package uk.gibby.example.schema

import UserCredentialsType
import UserSignUpDetailsType
import uk.gibby.dsl.core.scopeOf
import uk.gibby.dsl.functions.*
import uk.gibby.dsl.types.BooleanType.Companion.FALSE
import uk.gibby.dsl.types.eq
import user
import kotlin.time.Duration





val userScope = scopeOf(
    name = "user_scope",
    sessionDuration = Duration.parse("20m"),
    signupType = UserSignUpDetailsType,
    signInType = UserCredentialsType,
    tokenTable = user,
    signUp = {  auth ->
        user.create {
            it.username setAs auth.credentials.username
            it.passwordHash setAs Crypto.Argon2.generate(auth.credentials.password)
            it.details setAs auth.details
            it.isAdmin setAs false
        }
    },
    signIn = {  auth ->
        user.selectAll {
            where(
                it.username eq auth.username
                    and
                Crypto.Argon2.compare(it.passwordHash, auth.password)
            )
        }
    }
)

val adminScope = scopeOf(
    name = "admin_scope",
    sessionDuration = Duration.parse("20m"),
    signupType = UserSignUpDetailsType,
    signInType = UserCredentialsType,
    tokenTable = user,
    signUp = { user.selectAll { where(FALSE) }[0] },
    signIn = {  auth ->
        user.selectAll {
            where(
                Crypto.Argon2.compare(it.passwordHash, auth.password) and
                        (it.username eq auth.username) and it.isAdmin
            )
        }
    }

)
