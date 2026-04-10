import java.io.File
import lexer.Lexer
import parser.Parser
import runtime.Interpreter

fun main(args: Array<String>) {
    val source = when (args.size) {
        0 -> generateSequence(::readLine).joinToString("\n")
        1 -> File(args[0]).readText()
        else -> error("Expected zero or one argument: [path-to-source-file]")
    }

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