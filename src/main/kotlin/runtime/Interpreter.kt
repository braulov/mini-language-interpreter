package runtime

import ast.Expr
import ast.Stmt
import lexer.Token
import lexer.TokenType

class Interpreter : Expr.Visitor<Any>, Stmt.Visitor<Unit> {
    private val globals = Environment()
    private val functions = mutableMapOf<String, Stmt.Function>()

    private var environment: Environment = globals
    private var insideFunction: Boolean = false

    fun interpret(program: List<Stmt>): Map<String, Any> {
        for (statement in program) {
            execute(statement)
        }

        return globals.snapshot()
    }

    private fun execute(statement: Stmt) {
        statement.accept(this)
    }

    private fun evaluate(expression: Expr): Any {
        return expression.accept(this)
    }

    override fun visitAssignment(stmt: Stmt.Assignment) {
        val value = evaluate(stmt.value)
        environment.assignLocal(stmt.name.lexeme, value)
    }

    override fun visitIf(stmt: Stmt.If) {
        val condition = evaluate(stmt.condition)

        if (condition.asBoolean(tokenOf(stmt.condition))) {
            execute(stmt.thenBranch)
        } else {
            execute(stmt.elseBranch)
        }
    }

    override fun visitWhile(stmt: Stmt.While) {
        while (evaluate(stmt.condition).asBoolean(tokenOf(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visitBlock(stmt: Stmt.Block) {
        for (nested in stmt.statements) {
            execute(nested)
        }
    }

    override fun visitFunction(stmt: Stmt.Function) {
        functions[stmt.name.lexeme] = stmt
    }

    override fun visitReturn(stmt: Stmt.Return) {
        if (!insideFunction) {
            throw RuntimeError(stmt.keyword, "Return is only allowed inside a function.")
        }

        val value = evaluate(stmt.value)
        throw ReturnSignal(value)
    }

    override fun visitLiteral(expr: Expr.Literal): Any {
        return expr.value ?: throw RuntimeException("Null literals are not supported.")
    }

    override fun visitVariable(expr: Expr.Variable): Any {
        return environment.get(expr.name)
    }

    override fun visitGrouping(expr: Expr.Grouping): Any {
        return evaluate(expr.expression)
    }

    override fun visitUnary(expr: Expr.Unary): Any {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.MINUS -> -right.asInt(expr.operator)
            else -> throw RuntimeError(expr.operator, "Unsupported unary operator '${expr.operator.lexeme}'.")
        }
    }

    override fun visitBinary(expr: Expr.Binary): Any {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.PLUS -> left.asInt(expr.operator) + right.asInt(expr.operator)
            TokenType.MINUS -> left.asInt(expr.operator) - right.asInt(expr.operator)
            TokenType.STAR -> left.asInt(expr.operator) * right.asInt(expr.operator)

            TokenType.SLASH -> {
                val divisor = right.asInt(expr.operator)
                if (divisor == 0) {
                    throw RuntimeError(expr.operator, "Division by zero.")
                }
                left.asInt(expr.operator) / divisor
            }

            TokenType.EQUAL_EQUAL -> left == right
            TokenType.NOT_EQUAL -> left != right

            TokenType.LESS -> left.asInt(expr.operator) < right.asInt(expr.operator)
            TokenType.LESS_EQUAL -> left.asInt(expr.operator) <= right.asInt(expr.operator)
            TokenType.GREATER -> left.asInt(expr.operator) > right.asInt(expr.operator)
            TokenType.GREATER_EQUAL -> left.asInt(expr.operator) >= right.asInt(expr.operator)

            else -> throw RuntimeError(expr.operator, "Unsupported binary operator '${expr.operator.lexeme}'.")
        }
    }

    override fun visitCall(expr: Expr.Call): Any {
        val function = functions[expr.callee.lexeme]
            ?: throw RuntimeError(expr.callee, "Undefined function '${expr.callee.lexeme}'.")

        if (expr.arguments.size != function.parameters.size) {
            throw RuntimeError(
                expr.callee,
                "Function '${function.name.lexeme}' expected ${function.parameters.size} arguments but got ${expr.arguments.size}.",
            )
        }

        val callerEnvironment = environment
        val callerInsideFunction = insideFunction
        val localEnvironment = Environment(globals)

        for ((parameter, argumentExpression) in function.parameters.zip(expr.arguments)) {
            val argumentValue = evaluate(argumentExpression)
            localEnvironment.assignLocal(parameter.lexeme, argumentValue)
        }

        return try {
            environment = localEnvironment
            insideFunction = true
            execute(function.body)
            throw RuntimeError(function.name, "Function '${function.name.lexeme}' did not return a value.")
        } catch (signal: ReturnSignal) {
            signal.value
        } finally {
            environment = callerEnvironment
            insideFunction = callerInsideFunction
        }
    }

    private fun tokenOf(expr: Expr): Token {
        return when (expr) {
            is Expr.Literal -> expr.token
            is Expr.Variable -> expr.name
            is Expr.Unary -> expr.operator
            is Expr.Binary -> expr.operator
            is Expr.Call -> expr.callee
            is Expr.Grouping -> tokenOf(expr.expression)
        }
    }
}

private class ReturnSignal(
    val value: Any,
) : RuntimeException(null, null, false, false)

private fun Any.asInt(token: Token): Int {
    return this as? Int
        ?: throw RuntimeError(token, "Expected integer value, got ${this::class.simpleName}.")
}

private fun Any.asBoolean(token: Token): Boolean {
    return this as? Boolean
        ?: throw RuntimeError(token, "Expected boolean value, got ${this::class.simpleName}.")
}