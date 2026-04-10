package runtime

import ast.Stmt
import lexer.Lexer
import parser.Parser
import kotlin.test.Test
import kotlin.test.assertEquals

class InterpreterTest {

    @Test
    fun `interprets arithmetic assignments`() {
        val globals = interpret("""
            x = 2
            y = (x + 2) * 2
        """.trimIndent())

        assertEquals(2, globals["x"])
        assertEquals(8, globals["y"])
    }

    @Test
    fun `interprets if statement when condition is true`() {
        val globals = interpret("""
            x = 20
            if x > 10 then y = 100 else y = 0
        """.trimIndent())

        assertEquals(20, globals["x"])
        assertEquals(100, globals["y"])
    }

    @Test
    fun `interprets if statement when condition is false`() {
        val globals = interpret("""
            x = 5
            if x > 10 then y = 100 else y = 0
        """.trimIndent())

        assertEquals(5, globals["x"])
        assertEquals(0, globals["y"])
    }

    @Test
    fun `interprets nested if statements`() {
        val globals = interpret("""
            x = 10
            y = 0

            if x > 5 then
                if x > 8 then y = 1 else y = 2
            else
                y = 3
        """.trimIndent())

        assertEquals(10, globals["x"])
        assertEquals(1, globals["y"])
    }

    @Test
    fun `interprets nested if else branches correctly`() {
        val globals = interpret("""
            x = 7
            y = 0

            if x > 5 then
                if x > 8 then y = 1 else y = 2
            else
                y = 3
        """.trimIndent())

        assertEquals(7, globals["x"])
        assertEquals(2, globals["y"])
    }

    @Test
    fun `interprets if statement with brace block branches`() {
        val globals = interpret("""
            x = 10
            if x > 5 then {
                y = 1
                z = 2
            } else {
                y = 3
            }
        """.trimIndent())

        assertEquals(10, globals["x"])
        assertEquals(1, globals["y"])
        assertEquals(2, globals["z"])
    }

    @Test
    fun `interprets while loop with comma sequence body`() {
        val globals = interpret("""
            x = 0
            y = 0
            while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1
        """.trimIndent())

        assertEquals(3, globals["x"])
        assertEquals(11, globals["y"])
    }

    @Test
    fun `interprets while loop with brace block body`() {
        val globals = interpret("""
            x = 0
            y = 0

            while x < 3 do {
                y = y + 2
                x = x + 1
            }
        """.trimIndent())

        assertEquals(3, globals["x"])
        assertEquals(6, globals["y"])
    }

    @Test
    fun `interprets nested while loops with block syntax`() {
        val globals = interpret("""
            x = 0
            y = 0

            while x < 3 do {
                z = 0
                while z < 2 do {
                    y = y + 1
                    z = z + 1
                }
                x = x + 1
            }
        """.trimIndent())

        assertEquals(3, globals["x"])
        assertEquals(6, globals["y"])
        assertEquals(2, globals["z"])
    }

    @Test
    fun `interprets nested ifs with nested while loops`() {
        val globals = interpret("""
            x = 0
            y = 0

            if true then
                if true then
                    while x < 3 do {
                        z = 0
                        while z < 2 do {
                            y = y + 1
                            z = z + 1
                        }
                        x = x + 1
                    }
                else
                    y = 100
            else
                y = 200
        """.trimIndent())

        assertEquals(3, globals["x"])
        assertEquals(6, globals["y"])
        assertEquals(2, globals["z"])
    }

    @Test
    fun `interprets nested if and while with brace blocks`() {
        val globals = interpret("""
            x = 0
            y = 0

            if true then {
                while x < 3 do {
                    if x == 1 then {
                        y = y + 10
                    } else {
                        y = y + 1
                    }
                    x = x + 1
                }
            } else {
                y = 100
            }
        """.trimIndent())

        assertEquals(3, globals["x"])
        assertEquals(12, globals["y"])
    }

    @Test
    fun `interprets simple function call`() {
        val globals = interpret("""
            fun add(a, b) { return a + b }
            four = add(2, 2)
        """.trimIndent())

        assertEquals(4, globals["four"])
    }

    @Test
    fun `interprets recursive factorial`() {
        val globals = interpret("""
            fun fact_rec(n) { if n <= 0 then return 1 else return n * fact_rec(n - 1) }
            a = fact_rec(5)
        """.trimIndent())

        assertEquals(120, globals["a"])
    }

    @Test
    fun `interprets iterative factorial`() {
        val globals = interpret("""
            fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }
            b = fact_iter(5)
        """.trimIndent())

        assertEquals(120, globals["b"])
    }

    @Test
    fun `function can read global variable`() {
        val globals = interpret("""
            x = 10
            fun add_to_x(a) { return a + x }
            y = add_to_x(5)
        """.trimIndent())

        assertEquals(10, globals["x"])
        assertEquals(15, globals["y"])
    }

    @Test
    fun `function local assignment does not overwrite global variable`() {
        val globals = interpret("""
            x = 10
            fun f() { x = 99, return x }
            y = f()
        """.trimIndent())

        assertEquals(10, globals["x"])
        assertEquals(99, globals["y"])
    }

    @Test
    fun `function parameter shadows global variable`() {
        val globals = interpret("""
            x = 7
            fun f(x) { return x + 1 }
            y = f(20)
        """.trimIndent())

        assertEquals(7, globals["x"])
        assertEquals(21, globals["y"])
    }

    @Test
    fun `nested calls work correctly`() {
        val globals = interpret("""
            fun add(a, b) { return a + b }
            fun twice(x) { return add(x, x) }
            y = twice(6)
        """.trimIndent())

        assertEquals(12, globals["y"])
    }

    @Test
    fun `equality works for booleans`() {
        val globals = interpret("""
            x = true == false
            y = true != false
        """.trimIndent())

        assertEquals(false, globals["x"])
        assertEquals(true, globals["y"])
    }

    @Test
    fun `while loop can execute zero times`() {
        val globals = interpret("""
            x = 10
            while false do x = 20
        """.trimIndent())

        assertEquals(10, globals["x"])
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