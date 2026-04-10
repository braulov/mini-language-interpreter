package lexer

class Lexer(
    private val source: String,
) {
    private val tokens = mutableListOf<Token>()

    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens += Token(
            type = TokenType.EOF,
            lexeme = "",
            literal = null,
            line = line,
        )

        return tokens
    }

    private fun scanToken() {
        when (val c = advance()) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)

            '+' -> addToken(TokenType.PLUS)
            '-' -> addToken(TokenType.MINUS)
            '*' -> addToken(TokenType.STAR)
            '/' -> addToken(TokenType.SLASH)

            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.ASSIGN)
            '!' -> {
                if (match('=')) {
                    addToken(TokenType.NOT_EQUAL)
                } else {
                    throw IllegalArgumentException("Unexpected character '!' at line $line")
                }
            }
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)

            ' ', '\r', '\t' -> Unit

            '\n' -> {
                addToken(TokenType.NEWLINE)
                line++
            }

            else -> when {
                c.isDigit() -> number()
                isIdentifierStart(c) -> identifier()
                else -> throw IllegalArgumentException("Unexpected character '$c' at line $line")
            }
        }
    }

    private fun addToken(type: TokenType, literal: Any? = null) {
        val lexeme = source.substring(start, current)
        tokens += Token(
            type = type,
            lexeme = lexeme,
            literal = literal,
            line = line,
        )
    }

    private fun advance(): Char {
        return source[current++]
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    private fun peek(): Char? {
        return if (isAtEnd()) null else source[current]
    }

    private fun peekNext(): Char? {
        return if (current + 1 >= source.length) null else source[current + 1]
    }

    private fun number() {
        while (peek()?.isDigit() == true) {
            advance()
        }

        val lexeme = source.substring(start, current)
        addToken(
            type = TokenType.INT,
            literal = lexeme.toInt(),
        )
    }

    private fun identifier() {
        while (peek()?.let(::isIdentifierPart) == true) {
            advance()
        }

        val lexeme = source.substring(start, current)
        val type = keywords[lexeme] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun isIdentifierStart(c: Char): Boolean {
        return c.isLetter() || c == '_'
    }

    private fun isIdentifierPart(c: Char): Boolean {
        return c.isLetterOrDigit() || c == '_'
    }

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