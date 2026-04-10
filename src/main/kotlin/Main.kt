import lexer.Lexer
import parser.Parser
import runtime.Interpreter

fun main() {
    val source = generateSequence(::readLine).joinToString("\n")
    print(runProgram(source))
}

fun runProgram(source: String): String {
    val tokens = Lexer(source).scanTokens()
    val program = Parser(tokens).parseProgram()
    val globals = Interpreter().interpret(program)

    return globals.entries.joinToString("\n") { (name, value) ->
        "$name: $value"
    }
}