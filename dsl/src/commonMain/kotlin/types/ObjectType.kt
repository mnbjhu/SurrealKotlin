package types

interface ObjectType<T>: Reference<T> {
    fun <T, U: Reference<T>>attrOf(name: String, type: U): U {
        return type.createReference(this@ObjectType.getReference() + "." + name) as U
    }
    fun getFields(): Map<String, Reference<*>>
}