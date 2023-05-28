package functions

import types.BooleanType
import types.booleanType
infix fun BooleanType.and(other: BooleanType): BooleanType = booleanType.createReference("(${getReference()}) AND (${other.getReference()})")
infix fun BooleanType.or(other: BooleanType): BooleanType = booleanType.createReference("(${getReference()}) OR (${other.getReference()})")



