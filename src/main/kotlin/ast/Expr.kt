package ast

import lexer.Token

sealed interface Expr {
    fun <R> accept(visitor: Visitor<R>): R

    interface Visitor<R> {
        fun visitLiteral(expr: Literal): R
        fun visitVariable(expr: Variable): R
        fun visitGrouping(expr: Grouping): R
        fun visitUnary(expr: Unary): R
        fun visitBinary(expr: Binary): R
        fun visitCall(expr: Call): R
    }

    data class Literal(
        val token: Token,
        val value: Any?,
    ) : Expr {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitLiteral(this)
    }

    data class Variable(
        val name: Token,
    ) : Expr {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitVariable(this)
    }

    data class Grouping(
        val expression: Expr,
    ) : Expr {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitGrouping(this)
    }

    data class Unary(
        val operator: Token,
        val right: Expr,
    ) : Expr {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitUnary(this)
    }

    data class Binary(
        val left: Expr,
        val operator: Token,
        val right: Expr,
    ) : Expr {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitBinary(this)
    }

    data class Call(
        val callee: Token,
        val arguments: List<Expr>,
    ) : Expr {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visitCall(this)
    }
}