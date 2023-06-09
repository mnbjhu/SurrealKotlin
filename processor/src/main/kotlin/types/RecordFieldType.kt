package types

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toTypeName
import model.Linked

class RecordFieldType(private val innerType: KSType): SurrealFieldType {
    override fun getSurrealType(): TypeName {
        return RecordLink::class
            .asTypeName()
            .parameterizedBy(innerType.toTypeName(),
                ClassName(innerType.declaration.packageName.asString(), innerType.toTypeName().toString() + "Record")
            )
    }

    override fun getKotlinType(): TypeName {
        return Linked::class.asTypeName().parameterizedBy(innerType.toTypeName())
    }

    override fun getSurrealTypeFunction(): CodeBlock {
        val original = innerType.toTypeName().toString()
        val index = original.lastIndexOf(".")
        val name = if(index == -1){
            original.replaceFirstChar(Char::lowercaseChar)
        } else {
            original.take(index) + "." + original.takeLastWhile { it != '.' }.replaceFirstChar(Char::lowercaseChar)
        }


        return CodeBlock.builder()
            .add(CodeBlock.of("linked("))
            .add("%M)", MemberName(
                innerType.declaration.packageName.asString(),
                name
            )
            )
            .build()
    }

}