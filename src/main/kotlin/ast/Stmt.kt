package ast

import lexer.Token

sealed interface Stmt {
    data class Assignment(
        val name: Token,
        val value: Expr,
    ) : Stmt

    data class If(
        val condition: Expr,
        val thenBranch: Stmt,
        val elseBranch: Stmt,
    ) : Stmt

    data class While(
        val condition: Expr,
        val body: Stmt,
    ) : Stmt

    data class Block(
        val statements: List<Stmt>,
    ) : Stmt

    data class Function(
        val name: Token,
        val parameters: List<Token>,
        val body: Stmt,
    ) : Stmt

    data class Return(
        val keyword: Token,
        val value: Expr,
    ) : Stmt
}