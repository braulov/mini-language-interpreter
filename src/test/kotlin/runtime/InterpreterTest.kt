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

    @Test
    fun `function can read global variable`() {
        val program = parseProgram("""
        x = 10
        fun add_to_x(a) { return a + x }
        y = add_to_x(5)
    """.trimIndent())

        val globals = Interpreter().interpret(program)

        assertEquals(10, globals["x"])
        assertEquals(15, globals["y"])
    }

    @Test
    fun `function local assignment does not overwrite global variable`() {
        val program = parseProgram("""
        x = 10
        fun f() { x = 99, return x }
        y = f()
    """.trimIndent())

        val globals = Interpreter().interpret(program)

        assertEquals(10, globals["x"])
        assertEquals(99, globals["y"])
    }

    @Test
    fun `function parameter shadows global variable`() {
        val program = parseProgram("""
        x = 7
        fun f(x) { return x + 1 }
        y = f(20)
    """.trimIndent())

        val globals = Interpreter().interpret(program)

        assertEquals(7, globals["x"])
        assertEquals(21, globals["y"])
    }

    @Test
    fun `nested calls work correctly`() {
        val program = parseProgram("""
        fun add(a, b) { return a + b }
        fun twice(x) { return add(x, x) }
        y = twice(6)
    """.trimIndent())

        val globals = Interpreter().interpret(program)

        assertEquals(12, globals["y"])
    }

    @Test
    fun `equality works for booleans`() {
        val program = parseProgram("""
        x = true == false
        y = true != false
    """.trimIndent())

        val globals = Interpreter().interpret(program)

        assertEquals(false, globals["x"])
        assertEquals(true, globals["y"])
    }

    @Test
    fun `while loop can execute zero times`() {
        val program = parseProgram("""
        x = 10
        while false do x = 20
    """.trimIndent())

        val globals = Interpreter().interpret(program)

        assertEquals(10, globals["x"])
    }

    private fun parseProgram(source: String): List<Stmt> {
        val tokens = Lexer(source).scanTokens()
        return Parser(tokens).parseProgram()
    }
}