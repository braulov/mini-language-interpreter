package runtime

import ast.Stmt
import lexer.Lexer
import parser.ParseException
import parser.Parser
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class InterpreterNegativeTest {

    @Test
    fun `fails on undefined variable`() {
        val error = assertFailsWith<RuntimeError> {
            interpret("x = y")
        }

        assertTrue(error.message!!.contains("Undefined variable 'y'"))
    }

    @Test
    fun `fails on undefined function`() {
        val error = assertFailsWith<RuntimeError> {
            interpret("x = foo(1)")
        }

        assertTrue(error.message!!.contains("Undefined function 'foo'"))
    }

    @Test
    fun `fails on wrong argument count`() {
        val error = assertFailsWith<RuntimeError> {
            interpret("""
                fun add(a, b) { return a + b }
                x = add(1)
            """.trimIndent())
        }

        assertTrue(error.message!!.contains("expected 2 arguments but got 1"))
    }

    @Test
    fun `fails on return outside function`() {
        val error = assertFailsWith<RuntimeError> {
            interpret("return 1")
        }

        assertTrue(error.message!!.contains("Return is only allowed inside a function"))
    }

    @Test
    fun `fails when if condition is not boolean`() {
        val error = assertFailsWith<RuntimeError> {
            interpret("""
                x = 1
                if x then y = 2 else y = 3
            """.trimIndent())
        }

        assertTrue(error.message!!.contains("Expected boolean value"))
    }

    @Test
    fun `fails when while condition is not boolean`() {
        val error = assertFailsWith<RuntimeError> {
            interpret("""
                x = 1
                while x do x = 2
            """.trimIndent())
        }

        assertTrue(error.message!!.contains("Expected boolean value"))
    }

    @Test
    fun `fails when adding boolean to integer`() {
        val error = assertFailsWith<RuntimeError> {
            interpret("x = true + 1")
        }

        assertTrue(error.message!!.contains("Expected integer value"))
    }

    @Test
    fun `fails when unary minus is applied to boolean`() {
        val error = assertFailsWith<RuntimeError> {
            interpret("x = -true")
        }

        assertTrue(error.message!!.contains("Expected integer value"))
    }

    @Test
    fun `fails when function does not return`() {
        val error = assertFailsWith<RuntimeError> {
            interpret("""
                fun f() {}
                x = f()
            """.trimIndent())
        }

        assertTrue(error.message!!.contains("did not return a value"))
    }

    @Test
    fun `fails on division by zero`() {
        val error = assertFailsWith<RuntimeError> {
            interpret("x = 1 / 0")
        }

        assertTrue(error.message!!.contains("Division by zero"))
    }

    @Test
    fun `reports line number for literal condition type error`() {
        val error = assertFailsWith<RuntimeError> {
            interpret("""
                x = 1
                if 1 then y = 2 else y = 3
            """.trimIndent())
        }

        assertTrue(error.message!!.contains("line 2"))
    }

    @Test
    fun `fails on parser error for missing parenthesis`() {
        val error = assertFailsWith<ParseException> {
            parseProgram("x = (1 + 2")
        }

        assertTrue(error.message!!.contains("Expected ')' after expression"))
    }

    private fun interpret(source: String): Map<String, Any> {
        val tokens = Lexer(source).scanTokens()
        val program = Parser(tokens).parseProgram()
        return Interpreter().interpret(program)
    }

    private fun parseProgram(source: String): List<Stmt> {
        val tokens = Lexer(source).scanTokens()
        return Parser(tokens).parseProgram()
    }
}