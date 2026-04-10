package org.example.lexer

sealed interface Token {
    data class IntLiteral(val value: Int) : Token
    data class Identifier(val name: String) : Token

    data object True : Token
    data object False : Token

    data object Fun : Token
    data object Return : Token
    data object If : Token
    data object Then : Token
    data object Else : Token
    data object While : Token
    data object Do : Token

    data object Plus : Token
    data object Minus : Token
    data object Star : Token
    data object Slash : Token

    data object Assign : Token
    data object EqualEqual : Token
    data object NotEqual : Token
    data object Less : Token
    data object LessEqual : Token
    data object Greater : Token
    data object GreaterEqual : Token

    data object LeftParen : Token
    data object RightParen : Token
    data object LeftBrace : Token
    data object RightBrace : Token
    data object Comma : Token

    data object Newline : Token
    data object Eof : Token
}