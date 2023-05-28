package functions

import types.Reference
import types.longType

fun count(value: Reference<*>) = longType.createReference("count(${value.getReference()})")
