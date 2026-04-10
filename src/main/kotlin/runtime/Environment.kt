package runtime

class Environment {
    private val values = LinkedHashMap<String, Any>()

    fun assign(name: String, value: Any) {
        values[name] = value
    }

    fun get(name: String): Any {
        return values[name]
            ?: throw RuntimeException("Undefined variable '$name'.")
    }

    fun snapshot(): Map<String, Any> {
        return LinkedHashMap(values)
    }
}