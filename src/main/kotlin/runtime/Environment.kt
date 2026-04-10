package runtime

import lexer.Token

class Environment(
    private val enclosing: Environment? = null,
) {
    private val values = LinkedHashMap<String, Any>()

    fun assignLocal(name: String, value: Any) {
        values[name] = value
    }

    fun get(name: Token): Any {
        values[name.lexeme]?.let { return it }
        return enclosing?.get(name)
            ?: throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun snapshot(): Map<String, Any> {
        return LinkedHashMap(values)
    }
}