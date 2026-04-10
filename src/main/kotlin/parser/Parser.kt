package parser

import ast.Expr
import ast.Stmt
import lexer.Token
import lexer.TokenType

class Parser(
    private val tokens: List<Token>,
) {
    private var current = 0

    fun parseProgram(): List<Stmt> {
        val statements = mutableListOf<Stmt>()

        skipNewlines()

        while (!isAtEnd()) {
            statements += declaration()
            skipNewlines()
        }

        consume(TokenType.EOF, "Expected end of program.")
        return statements
    }

    fun parseExpression(): Expr {
        val expression = expression()
        consume(TokenType.EOF, "Expected end of expression.")
        return expression
    }

    private fun declaration(): Stmt {
        return if (match(TokenType.FUN)) {
            functionDeclaration()
        } else {
            statement()
        }
    }

    private fun functionDeclaration(): Stmt.Function {
        val name = consume(TokenType.IDENTIFIER, "Expected function name.")
        consume(TokenType.LEFT_PAREN, "Expected '(' after function name.")

        val parameters = mutableListOf<Token>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                parameters += consume(TokenType.IDENTIFIER, "Expected parameter name.")
            } while (match(TokenType.COMMA))
        }

        consume(TokenType.RIGHT_PAREN, "Expected ')' after parameters.")
        consume(TokenType.LEFT_BRACE, "Expected '{' before function body.")

        val body = if (check(TokenType.RIGHT_BRACE)) {
            Stmt.Block(emptyList())
        } else {
            parseBlockBody()
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after function body.")
        return Stmt.Function(name, parameters, body)
    }

    private fun parseBlockBody(): Stmt {
        val statements = mutableListOf<Stmt>()

        skipSeparators()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements += statement()
            skipSeparators()
        }

        return asBlockIfNeeded(statements)
    }

    private fun statement(): Stmt {
        return when {
            match(TokenType.IF) -> ifStatement()
            match(TokenType.WHILE) -> whileStatement()
            match(TokenType.RETURN) -> returnStatement()
            else -> assignmentStatement()
        }
    }

    private fun ifStatement(): Stmt.If {
        val condition = expression()
        consume(TokenType.THEN, "Expected 'then' after if condition.")
        val thenBranch = statement()
        consume(TokenType.ELSE, "Expected 'else' after then branch.")
        val elseBranch = statement()
        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun whileStatement(): Stmt.While {
        val condition = expression()
        consume(TokenType.DO, "Expected 'do' after while condition.")

        val body = if (match(TokenType.LEFT_BRACE)) {
            val block = if (check(TokenType.RIGHT_BRACE)) {
                Stmt.Block(emptyList())
            } else {
                parseBlockBody()
            }
            consume(TokenType.RIGHT_BRACE, "Expected '}' after while body.")
            block
        } else {
            parseInlineSequence()
        }

        return Stmt.While(condition, body)
    }

    private fun returnStatement(): Stmt.Return {
        val keyword = previous()
        val value = expression()
        return Stmt.Return(keyword, value)
    }

    private fun assignmentStatement(): Stmt.Assignment {
        val name = consume(TokenType.IDENTIFIER, "Expected variable name.")
        consume(TokenType.ASSIGN, "Expected '=' after variable name.")
        val value = expression()
        return Stmt.Assignment(name, value)
    }

    private fun parseInlineSequence(): Stmt {
        val statements = mutableListOf<Stmt>()
        statements += statement()

        while (match(TokenType.COMMA)) {
            statements += statement()
        }

        return asBlockIfNeeded(statements)
    }

    private fun asBlockIfNeeded(statements: List<Stmt>): Stmt =
        if (statements.size == 1) statements.single() else Stmt.Block(statements)

    private fun expression(): Expr = equality()

    private fun equality(): Expr {
        var expr = comparison()

        while (match(TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expr {
        var expr = term()

        while (
            match(
                TokenType.LESS,
                TokenType.LESS_EQUAL,
                TokenType.GREATER,
                TokenType.GREATER_EQUAL,
            )
        ) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expr {
        var expr = factor()

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expr {
        var expr = unary()

        while (match(TokenType.STAR, TokenType.SLASH)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expr {
        if (match(TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return call()
    }

    private fun call(): Expr {
        var expr = primary()

        while (true) {
            expr = when {
                match(TokenType.LEFT_PAREN) -> finishCall(expr)
                else -> return expr
            }
        }
    }

    private fun finishCall(calleeExpr: Expr): Expr {
        val callee = calleeExpr as? Expr.Variable
            ?: throw errorAt(previous(), "Only named function calls are supported.")

        val arguments = mutableListOf<Expr>()

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                arguments += expression()
            } while (match(TokenType.COMMA))
        }

        consume(TokenType.RIGHT_PAREN, "Expected ')' after arguments.")
        return Expr.Call(callee.name, arguments)
    }

    private fun primary(): Expr {
        when {
            match(TokenType.FALSE) -> return Expr.Literal(false)
            match(TokenType.TRUE) -> return Expr.Literal(true)
            match(TokenType.INT) -> return Expr.Literal(previous().literal)
            match(TokenType.IDENTIFIER) -> return Expr.Variable(previous())
            match(TokenType.LEFT_PAREN) -> {
                val expr = expression()
                consume(TokenType.RIGHT_PAREN, "Expected ')' after expression.")
                return Expr.Grouping(expr)
            }
            else -> throw errorAt(peek(), "Expected expression.")
        }
    }

    private fun skipNewlines() {
        while (match(TokenType.NEWLINE)) Unit
    }

    private fun skipSeparators() {
        while (match(TokenType.NEWLINE, TokenType.COMMA)) Unit
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw errorAt(peek(), message)
    }

    private fun check(type: TokenType): Boolean = peek().type == type

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun errorAt(token: Token, message: String): ParseException {
        return ParseException(token.line, message)
    }
}

class ParseException(
    line: Int,
    message: String,
) : RuntimeException("Parse error at line $line: $message")