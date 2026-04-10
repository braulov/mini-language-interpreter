package org.example

import org.example.lexer.Token

fun main() {
    val program = generateSequence(::readLine).joinToString("\n")
    println(runProgram(program))
}

fun runProgram(source: String): String = TODO("Interpreter is not implemented yet")