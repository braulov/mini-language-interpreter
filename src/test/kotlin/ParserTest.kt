package parser

import ast.Expr
import lexer.Lexer
import lexer.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ParserTest {

    @Test
    fun `parses integer literal`() {
        val expr = parseExpression("123")

        val literal = assertIs<Expr.Literal>(expr)
        assertEquals(123, literal.value)
    }

    @Test
    fun `parses boolean literal`() {
        val expr = parseExpression("true")

        val literal = assertIs<Expr.Literal>(expr)
        assertEquals(true, literal.value)
    }

    @Test
    fun `parses variable`() {
        val expr = parseExpression("abc")

        val variable = assertIs<Expr.Variable>(expr)
        assertEquals("abc", variable.name.lexeme)
    }

    @Test
    fun `parses grouping`() {
        val expr = parseExpression("(1)")

        val grouping = assertIs<Expr.Grouping>(expr)
        val literal = assertIs<Expr.Literal>(grouping.expression)
        assertEquals(1, literal.value)
    }

    @Test
    fun `parses unary minus`() {
        val expr = parseExpression("-123")

        val unary = assertIs<Expr.Unary>(expr)
        assertEquals(TokenType.MINUS, unary.operator.type)

        val literal = assertIs<Expr.Literal>(unary.right)
        assertEquals(123, literal.value)
    }

    @Test
    fun `parses multiplication before addition`() {
        val expr = parseExpression("1 + 2 * 3")

        val binary = assertIs<Expr.Binary>(expr)
        assertEquals(TokenType.PLUS, binary.operator.type)

        val left = assertIs<Expr.Literal>(binary.left)
        assertEquals(1, left.value)

        val right = assertIs<Expr.Binary>(binary.right)
        assertEquals(TokenType.STAR, right.operator.type)

        val rightLeft = assertIs<Expr.Literal>(right.left)
        assertEquals(2, rightLeft.value)

        val rightRight = assertIs<Expr.Literal>(right.right)
        assertEquals(3, rightRight.value)
    }

    @Test
    fun `parses comparison after arithmetic`() {
        val expr = parseExpression("1 + 2 <= 4")

        val binary = assertIs<Expr.Binary>(expr)
        assertEquals(TokenType.LESS_EQUAL, binary.operator.type)

        val left = assertIs<Expr.Binary>(binary.left)
        assertEquals(TokenType.PLUS, left.operator.type)

        val right = assertIs<Expr.Literal>(binary.right)
        assertEquals(4, right.value)
    }

    @Test
    fun `parses function call`() {
        val expr = parseExpression("add(1, x, 3)")

        val call = assertIs<Expr.Call>(expr)
        assertEquals("add", call.callee.lexeme)
        assertEquals(3, call.arguments.size)

        val first = assertIs<Expr.Literal>(call.arguments[0])
        assertEquals(1, first.value)

        val second = assertIs<Expr.Variable>(call.arguments[1])
        assertEquals("x", second.name.lexeme)

        val third = assertIs<Expr.Literal>(call.arguments[2])
        assertEquals(3, third.value)
    }

    private fun parseExpression(source: String): Expr {
        val tokens = Lexer(source).scanTokens()
        return Parser(tokens).parseExpression()
    }
}