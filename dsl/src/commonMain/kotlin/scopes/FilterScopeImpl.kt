package scopes

import model.Linked
import types.BooleanType
import types.ListType
import types.RecordLink
import types.RecordType

class FilterScopeImpl<T, U: RecordType<T>>(override val type: U): FilterScope, ReturningScope<T, U> {
    private var where: String? = null
    private var fetch: String? = null
    override fun getFilterString(): String {
        var r = ""
        if(where != null) r += " WHERE $where"
        if(fetch != null) r += " FETCH $fetch"
        return r

    }

    override fun where(condition: BooleanType): UnitType {
        where = condition.getReference()
        return None
    }

    override fun <T, U : RecordType<T>> fetch(items: ListType<Linked<T>, RecordLink<T, U>>): UnitType {
        fetch = items.getReference()
        return None
    }

}