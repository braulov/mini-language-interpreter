package lexer

import kotlin.test.Test
import kotlin.test.assertEquals

class LexerTest {

    @Test
    fun `scans assignment and arithmetic`() {
        val source = """
            x = 2
            y = (x + 2) * 2
        """.trimIndent()

        val tokens = Lexer(source).scanTokens()

        assertTokenTypes(
            tokens,
            TokenType.IDENTIFIER,
            TokenType.ASSIGN,
            TokenType.INT,
            TokenType.NEWLINE,
            TokenType.IDENTIFIER,
            TokenType.ASSIGN,
            TokenType.LEFT_PAREN,
            TokenType.IDENTIFIER,
            TokenType.PLUS,
            TokenType.INT,
            TokenType.RIGHT_PAREN,
            TokenType.STAR,
            TokenType.INT,
            TokenType.EOF,
        )

        assertEquals("x", tokens[0].lexeme)
        assertEquals(2, tokens[2].literal)
        assertEquals("y", tokens[4].lexeme)
        assertEquals(2, tokens[9].literal)
        assertEquals(2, tokens[12].literal)
    }

    @Test
    fun `scans keywords and comparisons`() {
        val source = "if x <= 10 then return true else return false"

        val tokens = Lexer(source).scanTokens()

        assertTokenTypes(
            tokens,
            TokenType.IF,
            TokenType.IDENTIFIER,
            TokenType.LESS_EQUAL,
            TokenType.INT,
            TokenType.THEN,
            TokenType.RETURN,
            TokenType.TRUE,
            TokenType.ELSE,
            TokenType.RETURN,
            TokenType.FALSE,
            TokenType.EOF,
        )

        assertEquals("x", tokens[1].lexeme)
        assertEquals(10, tokens[3].literal)
    }

    @Test
    fun `scans function declaration`() {
        val source = "fun add(a, b) { return a + b }"

        val tokens = Lexer(source).scanTokens()

        assertTokenTypes(
            tokens,
            TokenType.FUN,
            TokenType.IDENTIFIER,
            TokenType.LEFT_PAREN,
            TokenType.IDENTIFIER,
            TokenType.COMMA,
            TokenType.IDENTIFIER,
            TokenType.RIGHT_PAREN,
            TokenType.LEFT_BRACE,
            TokenType.RETURN,
            TokenType.IDENTIFIER,
            TokenType.PLUS,
            TokenType.IDENTIFIER,
            TokenType.RIGHT_BRACE,
            TokenType.EOF,
        )

        assertEquals("add", tokens[1].lexeme)
        assertEquals("a", tokens[3].lexeme)
        assertEquals("b", tokens[5].lexeme)
    }

    @Test
    fun `scans while with equality and comma`() {
        val source = "while x == 1 do y = y + 1, x = x + 1"

        val tokens = Lexer(source).scanTokens()

        assertTokenTypes(
            tokens,
            TokenType.WHILE,
            TokenType.IDENTIFIER,
            TokenType.EQUAL_EQUAL,
            TokenType.INT,
            TokenType.DO,
            TokenType.IDENTIFIER,
            TokenType.ASSIGN,
            TokenType.IDENTIFIER,
            TokenType.PLUS,
            TokenType.INT,
            TokenType.COMMA,
            TokenType.IDENTIFIER,
            TokenType.ASSIGN,
            TokenType.IDENTIFIER,
            TokenType.PLUS,
            TokenType.INT,
            TokenType.EOF,
        )

        assertEquals(1, tokens[3].literal)
        assertEquals(1, tokens[9].literal)
        assertEquals(1, tokens[15].literal)
    }

    @Test
    fun `scans consecutive newlines`() {
        val source = "x = 1\n\n\ny = 2"

        val tokens = Lexer(source).scanTokens()

        assertTokenTypes(
            tokens,
            TokenType.IDENTIFIER,
            TokenType.ASSIGN,
            TokenType.INT,
            TokenType.NEWLINE,
            TokenType.NEWLINE,
            TokenType.NEWLINE,
            TokenType.IDENTIFIER,
            TokenType.ASSIGN,
            TokenType.INT,
            TokenType.EOF,
        )
    }

    private fun assertTokenTypes(tokens: List<Token>, vararg expected: TokenType) {
        assertEquals(expected.toList(), tokens.map(Token::type))
    }
}