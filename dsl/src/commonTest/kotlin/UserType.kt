import core.Table
import kotlinx.serialization.Serializable
import types.*
import kotlin.String
import kotlin.jvm.JvmInline


@Serializable
data class User(val username: String, val passwordHash: String)
@JvmInline
public value class UserRecord(
  private val reference: String,
) : RecordType<User> {
  public override val id: RecordLink<User, UserRecord>
    get() = id()

  public val username: StringType
    get() = attrOf("username", stringType)

  public val passwordHash: StringType
    get() = attrOf("passwordHash", stringType)

  public override fun createReference(ref: String): UserRecord = UserRecord(ref)

  public override fun getReference(): String = reference
  override fun getFields(): Map<String, Reference<*>> {
    return mapOf(
      "username" to username,
      "passwordHash" to passwordHash
    )
  }
}

public val user: Table<User, UserRecord> = Table("User", UserRecord("_"))
