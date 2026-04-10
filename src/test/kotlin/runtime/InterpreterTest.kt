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

    private fun parseProgram(source: String): List<Stmt> {
        val tokens = Lexer(source).scanTokens()
        return Parser(tokens).parseProgram()
    }
}