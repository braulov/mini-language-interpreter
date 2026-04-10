import kotlin.test.Test
import kotlin.test.assertEquals

class LexerTest {

    @Test
    fun `lexes assignment and arithmetic`() {
        val source = """
            x = 2
            y = (x + 2) * 2
        """.trimIndent()

        val expected = listOf(
            token.Identifier("x"),
            token.Assign,
            token.IntLiteral(2),
            token.Newline,
            token.Identifier("y"),
            token.Assign,
            token.LeftParen,
            token.Identifier("x"),
            token.Plus,
            token.IntLiteral(2),
            token.RightParen,
            token.Star,
            token.IntLiteral(2),
            token.Eof
        )

        assertEquals(expected, lex(source))
    }

    @Test
    fun `lexes keywords and comparisons`() {
        val source = "if x <= 10 then return true else return false"

        val expected = listOf(
            token.If,
            token.Identifier("x"),
            token.LessEqual,
            token.IntLiteral(10),
            token.Then,
            token.Return,
            token.True,
            token.Else,
            token.Return,
            token.False,
            token.Eof
        )

        assertEquals(expected, lex(source))
    }

    @Test
    fun `lexes function declaration`() {
        val source = "fun add(a, b) { return a + b }"

        val expected = listOf(
            token.Fun,
            token.Identifier("add"),
            token.LeftParen,
            token.Identifier("a"),
            token.Comma,
            token.Identifier("b"),
            token.RightParen,
            token.LeftBrace,
            token.Return,
            token.Identifier("a"),
            token.Plus,
            token.Identifier("b"),
            token.RightBrace,
            token.Eof
        )

        assertEquals(expected, lex(source))
    }

    @Test
    fun `lexes while with equality and comma`() {
        val source = "while x == 1 do y = y + 1, x = x + 1"

        val expected = listOf(
            token.While,
            token.Identifier("x"),
            token.EqualEqual,
            token.IntLiteral(1),
            token.Do,
            token.Identifier("y"),
            token.Assign,
            token.Identifier("y"),
            token.Plus,
            token.IntLiteral(1),
            token.Comma,
            token.Identifier("x"),
            token.Assign,
            token.Identifier("x"),
            token.Plus,
            token.IntLiteral(1),
            token.Eof
        )

        assertEquals(expected, lex(source))
    }

    @Test
    fun `collapses consecutive newlines into separate newline tokens`() {
        val source = "x = 1\n\n\ny = 2"

        val expected = listOf(
            token.Identifier("x"),
            token.Assign,
            token.IntLiteral(1),
            token.Newline,
            token.Newline,
            token.Newline,
            token.Identifier("y"),
            token.Assign,
            token.IntLiteral(2),
            token.Eof
        )

        assertEquals(expected, lex(source))
    }
}