package runtime

class Environment(
    private val enclosing: Environment? = null,
) {
    private val values = LinkedHashMap<String, Any>()

    fun assignLocal(name: String, value: Any) {
        values[name] = value
    }

    fun get(name: String): Any {
        values[name]?.let { return it }
        return enclosing?.get(name)
            ?: throw RuntimeException("Undefined variable '$name'.")
    }

    fun snapshot(): Map<String, Any> {
        return LinkedHashMap(values)
    }
}