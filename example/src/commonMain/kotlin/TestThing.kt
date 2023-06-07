import annotation.Object
import annotation.Table

@Object
data class TestThing(val id: String, val number: Long)

@Table
data class User(val username: String, val passwordHash: String)

@Table
data class UserTable(val userId: Long, val username: String, val password: String)
