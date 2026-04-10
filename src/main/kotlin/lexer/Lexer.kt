package lexer

class Lexer(
    private val source: String,
) {
    private val tokens = mutableListOf<Token>()

    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> = TODO("Not yet implemented")

    private fun scanToken() : Nothing = TODO("Not yet implemented")

    private fun addToken(type: TokenType, literal: Any? = null): Nothing =
        TODO("Not yet implemented")

    private fun advance(): Char = TODO("Not yet implemented")

    private fun match(expected: Char): Boolean =
        TODO("Not yet implemented")

    private fun peek(): Char? = TODO("Not yet implemented")

    private fun peekNext(): Char? = TODO("Not yet implemented")

    private fun number(): Nothing = TODO("Not yet implemented")

    private fun identifier(): Nothing = TODO("Not yet implemented")

    private fun isAtEnd(): Boolean = TODO("Not yet implemented")

    private companion object {
        val keywords = mapOf(
            "fun" to TokenType.FUN,
            "return" to TokenType.RETURN,
            "if" to TokenType.IF,
            "then" to TokenType.THEN,
            "else" to TokenType.ELSE,
            "while" to TokenType.WHILE,
            "do" to TokenType.DO,
            "true" to TokenType.TRUE,
            "false" to TokenType.FALSE,
        )
    }
}