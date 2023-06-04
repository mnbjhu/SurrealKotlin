package scopes

import types.Reference
import kotlin.reflect.KProperty


class CodeBlockScope {
    private var generated: String = "{\n"
    operator fun Reference<*>.unaryPlus(){
        generated += "\t${getReference()}"
        generated += ";\n"
    }
    fun getBlockText() = "$generated}"

    @Suppress("UNCHECKED_CAST")
    operator fun <T, U: Reference<T>>U.getValue(thisRef: Any?, property: KProperty<*>): U =
        this.createReference("\$${property.name}").also { +createReference("LET \$${property.name} = ${getReference()}") } as U
}
