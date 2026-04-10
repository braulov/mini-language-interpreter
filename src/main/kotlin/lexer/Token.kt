package lexer

data class Token(
    val type: TokenType,
    val lexeme: String,
    val literal: Any? = null,
    val line: Int,
)

enum class TokenType {
    IDENTIFIER, INT,

    TRUE, FALSE, FUN, RETURN,
    IF, THEN, ELSE, WHILE,
    DO,

    PLUS, MINUS, STAR, SLASH,

    ASSIGN, EQUAL_EQUAL, NOT_EQUAL,
    LESS, LESS_EQUAL, GREATER,
    GREATER_EQUAL,

    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    COMMA,

    NEWLINE, EOF,
}