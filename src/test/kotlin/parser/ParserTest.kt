package parser

import ast.Expr
import ast.Stmt
import lexer.Lexer
import lexer.TokenType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ParserTest {

    @Test
    fun `parses integer literal`() {
        val expr = parseExpression("123")

        val literal = assertIs<Expr.Literal>(expr)
        assertEquals(123, literal.value)
        assertEquals(1, literal.token.line)
    }

    @Test
    fun `parses boolean literal`() {
        val expr = parseExpression("true")

        val literal = assertIs<Expr.Literal>(expr)
        assertEquals(true, literal.value)
        assertEquals(TokenType.TRUE, literal.token.type)
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
    fun `parses chained arithmetic left associatively`() {
        val expr = parseExpression("1 - 2 - 3")

        val outer = assertIs<Expr.Binary>(expr)
        assertEquals(TokenType.MINUS, outer.operator.type)

        val left = assertIs<Expr.Binary>(outer.left)
        assertEquals(TokenType.MINUS, left.operator.type)

        val leftLeft = assertIs<Expr.Literal>(left.left)
        assertEquals(1, leftLeft.value)

        val leftRight = assertIs<Expr.Literal>(left.right)
        assertEquals(2, leftRight.value)

        val right = assertIs<Expr.Literal>(outer.right)
        assertEquals(3, right.value)
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

    @Test
    fun `parses nested function calls`() {
        val expr = parseExpression("f(g(1), h(2, 3))")

        val call = assertIs<Expr.Call>(expr)
        assertEquals("f", call.callee.lexeme)
        assertEquals(2, call.arguments.size)

        val first = assertIs<Expr.Call>(call.arguments[0])
        assertEquals("g", first.callee.lexeme)

        val second = assertIs<Expr.Call>(call.arguments[1])
        assertEquals("h", second.callee.lexeme)
        assertEquals(2, second.arguments.size)
    }

    @Test
    fun `parses assignment statement`() {
        val program = parseProgram("x = 42")

        assertEquals(1, program.size)

        val stmt = assertIs<Stmt.Assignment>(program[0])
        assertEquals("x", stmt.name.lexeme)

        val value = assertIs<Expr.Literal>(stmt.value)
        assertEquals(42, value.value)
    }

    @Test
    fun `parses if statement`() {
        val program = parseProgram("if true then x = 1 else x = 2")

        assertEquals(1, program.size)

        val stmt = assertIs<Stmt.If>(program[0])

        val condition = assertIs<Expr.Literal>(stmt.condition)
        assertEquals(true, condition.value)

        val thenBranch = assertIs<Stmt.Assignment>(stmt.thenBranch)
        assertEquals("x", thenBranch.name.lexeme)

        val elseBranch = assertIs<Stmt.Assignment>(stmt.elseBranch)
        assertEquals("x", elseBranch.name.lexeme)
    }

    @Test
    fun `parses nested if statements`() {
        val program = parseProgram(
            "if true then if false then x = 1 else x = 2 else x = 3"
        )

        val outer = assertIs<Stmt.If>(program.single())
        val inner = assertIs<Stmt.If>(outer.thenBranch)
        assertIs<Stmt.Assignment>(inner.thenBranch)
        assertIs<Stmt.Assignment>(inner.elseBranch)
        assertIs<Stmt.Assignment>(outer.elseBranch)
    }

    @Test
    fun `parses while with comma sequence body as block`() {
        val program = parseProgram("while x < 3 do y = y + 1, x = x + 1")

        assertEquals(1, program.size)

        val stmt = assertIs<Stmt.While>(program[0])
        val body = assertIs<Stmt.Block>(stmt.body)
        assertEquals(2, body.statements.size)

        assertIs<Stmt.Assignment>(body.statements[0])
        assertIs<Stmt.Assignment>(body.statements[1])
    }

    @Test
    fun `parses while statement with brace block body`() {
        val program = parseProgram("""
            while x < 3 do {
                y = y + 1
                x = x + 1
            }
        """.trimIndent())

        val stmt = assertIs<Stmt.While>(program.single())
        val body = assertIs<Stmt.Block>(stmt.body)
        assertEquals(2, body.statements.size)
    }

    @Test
    fun `parses while statement with brace block body after newline`() {
        val program = parseProgram("""
            while x < 3 do
            {
                y = y + 1
                x = x + 1
            }
        """.trimIndent())

        val stmt = assertIs<Stmt.While>(program.single())
        val body = assertIs<Stmt.Block>(stmt.body)
        assertEquals(2, body.statements.size)
    }

    @Test
    fun `parses function declaration`() {
        val program = parseProgram("fun add(a, b) { return a + b }")

        assertEquals(1, program.size)

        val stmt = assertIs<Stmt.Function>(program[0])
        assertEquals("add", stmt.name.lexeme)
        assertEquals(listOf("a", "b"), stmt.parameters.map { it.lexeme })

        val body = assertIs<Stmt.Return>(stmt.body)
        val expr = assertIs<Expr.Binary>(body.value)
        assertEquals(TokenType.PLUS, expr.operator.type)
    }

    @Test
    fun `parses function with empty body`() {
        val program = parseProgram("fun f() {}")

        val function = assertIs<Stmt.Function>(program.single())
        val body = assertIs<Stmt.Block>(function.body)
        assertEquals(0, body.statements.size)
    }

    @Test
    fun `parses multi statement function body as block`() {
        val program = parseProgram("""
            fun f(n) {
                x = 1
                return n
            }
        """.trimIndent())

        assertEquals(1, program.size)

        val function = assertIs<Stmt.Function>(program[0])
        val body = assertIs<Stmt.Block>(function.body)
        assertEquals(2, body.statements.size)
        assertIs<Stmt.Assignment>(body.statements[0])
        assertIs<Stmt.Return>(body.statements[1])
    }

    @Test
    fun `parses block body with commas and newlines`() {
        val program = parseProgram("""
            fun f() {
                x = 1,
                y = 2
                return y
            }
        """.trimIndent())

        val function = assertIs<Stmt.Function>(program.single())
        val body = assertIs<Stmt.Block>(function.body)
        assertEquals(3, body.statements.size)
    }

    @Test
    fun `parses top level declarations separated by newlines`() {
        val program = parseProgram("""
            x = 1
            y = 2
        """.trimIndent())

        assertEquals(2, program.size)
        assertIs<Stmt.Assignment>(program[0])
        assertIs<Stmt.Assignment>(program[1])
    }

    @Test
    fun `parses sample while if body with correct outer sequencing`() {
        val program = parseProgram("""
            while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1
        """.trimIndent())

        assertEquals(1, program.size)

        val whileStmt = assertIs<Stmt.While>(program[0])
        val body = assertIs<Stmt.Block>(whileStmt.body)
        assertEquals(2, body.statements.size)

        assertIs<Stmt.If>(body.statements[0])
        assertIs<Stmt.Assignment>(body.statements[1])
    }

    @Test
    fun `fails on assignment without equals`() {
        val error = assertFailsWith<ParseException> {
            parseProgram("x 1")
        }

        assertTrue(error.message!!.contains("Expected '=' after variable name"))
    }

    private fun parseExpression(source: String): Expr {
        val tokens = Lexer(source).scanTokens()
        return Parser(tokens).parseExpression()
    }

    private fun parseProgram(source: String): List<Stmt> {
        val tokens = Lexer(source).scanTokens()
        return Parser(tokens).parseProgram()
    }
}