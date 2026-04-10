package runtime

import ast.Stmt
import lexer.Lexer
import parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class InterpreterTest {

    @Test
    fun `interprets arithmetic assignments`() {
        val program = parseProgram("""
            x = 2
            y = (x + 2) * 2
        """.trimIndent())

        val globals = Interpreter().interpret(program)

        assertEquals(2, globals["x"])
        assertEquals(8, globals["y"])
    }

    @Test
    fun `interprets if statement when condition is true`() {
        val program = parseProgram("""
            x = 20
            if x > 10 then y = 100 else y = 0
        """.trimIndent())

        val globals = Interpreter().interpret(program)

        assertEquals(20, globals["x"])
        assertEquals(100, globals["y"])
    }

    @Test
    fun `interprets if statement when condition is false`() {
        val program = parseProgram("""
            x = 5
            if x > 10 then y = 100 else y = 0
        """.trimIndent())

        val globals = Interpreter().interpret(program)

        assertEquals(5, globals["x"])
        assertEquals(0, globals["y"])
    }

    @Test
    fun `interprets while loop with comma sequence body`() {
        val program = parseProgram("""
            x = 0
            y = 0
            while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1
        """.trimIndent())

        val globals = Interpreter().interpret(program)

        assertEquals(3, globals["x"])
        assertEquals(11, globals["y"])
    }

    @Test
    fun `interprets explicit block body`() {
        val program = listOf(
            Stmt.Block(
                listOf(
                    Stmt.Assignment(
                        name = lexer.Token(lexer.TokenType.IDENTIFIER, "x", null, 1),
                        value = ast.Expr.Literal(1),
                    ),
                    Stmt.Assignment(
                        name = lexer.Token(lexer.TokenType.IDENTIFIER, "y", null, 1),
                        value = ast.Expr.Literal(2),
                    ),
                )
            )
        )

        val globals = Interpreter().interpret(program)

        assertEquals(1, globals["x"])
        assertEquals(2, globals["y"])
    }

    private fun parseProgram(source: String): List<Stmt> {
        val tokens = Lexer(source).scanTokens()
        return Parser(tokens).parseProgram()
    }
}