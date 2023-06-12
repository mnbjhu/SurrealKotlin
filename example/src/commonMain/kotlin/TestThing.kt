import kotlinx.datetime.Instant
import uk.gibby.dsl.annotation.Object
import uk.gibby.dsl.annotation.Relation
import uk.gibby.dsl.annotation.Table
import uk.gibby.dsl.model.Linked

@Table
data class Person(
    val name: String,
    val dateOfBirth: Instant
)

@Table
data class Movie(
    val title: String,
    val released: Instant,
    val genres: List<Linked<Genre>>,
    val rating: Double
)

@Table
data class Genre(val name: String)

@Relation<Person, Movie>
class Directed

@Relation<Person, Movie>
data class ActedIn(val role: String)

@Table
data class User(
    val username: String,
    val passwordHash: String,
    val details: UserDetails,
    val isAdmin: Boolean
)

@Object
data class UserDetails(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: Instant,
    val email: String,
    val phoneNumber: String,
)


@Object
class UserCredentials(
    val username: String,
    val password: String
)

@Object
class UserSignUpDetails(
    val credentials: UserCredentials,
    val details: UserDetails
)
