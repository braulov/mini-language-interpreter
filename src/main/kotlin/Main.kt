package org.example

fun main() {
    val program = generateSequence(::readLine).joinToString("\n")
    if (program.isNotBlank()) {
        error("Interpreter is not implemented yet")
    }
}