import kotlin.test.Test
import kotlin.test.assertEquals

class AcceptanceTest {

    @Test
    fun `assignment and arithmetic`() {
        val source = """
            x = 2
            y = (x + 2) * 2
        """.trimIndent()

        val expected = """
            x: 2
            y: 8
        """.trimIndent()

        assertEquals(expected, runProgram(source))
    }

    @Test
    fun `if statement`() {
        val source = """
            x = 20
            if x > 10 then y = 100 else y = 0
        """.trimIndent()

        val expected = """
            x: 20
            y: 100
        """.trimIndent()

        assertEquals(expected, runProgram(source))
    }

    @Test
    fun `while loop with sequence`() {
        val source = """
            x = 0
            y = 0
            while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1
        """.trimIndent()

        val expected = """
            x: 3
            y: 11
        """.trimIndent()

        assertEquals(expected, runProgram(source))
    }

    @Test
    fun `function call`() {
        val source = """
            fun add(a, b) { return a + b }
            four = add(2, 2)
        """.trimIndent()

        val expected = """
            four: 4
        """.trimIndent()

        assertEquals(expected, runProgram(source))
    }

    @Test
    fun `recursive factorial`() {
        val source = """
            fun fact_rec(n) { if n <= 0 then return 1 else return n * fact_rec(n - 1) }
            a = fact_rec(5)
        """.trimIndent()

        val expected = """
            a: 120
        """.trimIndent()

        assertEquals(expected, runProgram(source))
    }

    @Test
    fun `iterative factorial`() {
        val source = """
            fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }
            b = fact_iter(5)
        """.trimIndent()

        val expected = """
            b: 120
        """.trimIndent()

        assertEquals(expected, runProgram(source))
    }
}