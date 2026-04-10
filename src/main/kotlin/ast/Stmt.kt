package ast

import lexer.Token

sealed interface Stmt {
    fun <R> accept(visitor: Visitor<R>): R

    interface Visitor<R> {
        fun visitAssignment(stmt: Assignment): R
        fun visitIf(stmt: If): R
        fun visitWhile(stmt: While): R
        fun visitBlock(stmt: Block): R
        fun visitFunction(stmt: Function): R
        fun visitReturn(stmt: Return): R
    }

    data class Assignment(
        val name: Token,
        val value: Expr,
    ) : Stmt {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitAssignment(this)
    }

    data class If(
        val condition: Expr,
        val thenBranch: Stmt,
        val elseBranch: Stmt,
    ) : Stmt {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitIf(this)
    }

    data class While(
        val condition: Expr,
        val body: Stmt,
    ) : Stmt {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitWhile(this)
    }

    data class Block(
        val statements: List<Stmt>,
    ) : Stmt {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitBlock(this)
    }

    data class Function(
        val name: Token,
        val parameters: List<Token>,
        val body: Stmt,
    ) : Stmt {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitFunction(this)
    }

    data class Return(
        val keyword: Token,
        val value: Expr,
    ) : Stmt {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitReturn(this)
    }
}