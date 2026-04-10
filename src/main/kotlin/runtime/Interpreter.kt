package runtime

import ast.Expr
import ast.Stmt
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

        if (condition.asBoolean()) {
            execute(stmt.thenBranch)
        } else {
            execute(stmt.elseBranch)
        }
    }

    override fun visitWhile(stmt: Stmt.While) {
        while (evaluate(stmt.condition).asBoolean()) {
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
            throw RuntimeException("Return is only allowed inside a function.")
        }

        val value = evaluate(stmt.value)
        throw ReturnSignal(value)
    }

    override fun visitLiteral(expr: Expr.Literal): Any {
        return expr.value
            ?: throw RuntimeException("Null literals are not supported.")
    }

    override fun visitVariable(expr: Expr.Variable): Any {
        return environment.get(expr.name.lexeme)
    }

    override fun visitGrouping(expr: Expr.Grouping): Any {
        return evaluate(expr.expression)
    }

    override fun visitUnary(expr: Expr.Unary): Any {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.MINUS -> -right.asInt()
            else -> throw RuntimeException("Unsupported unary operator: ${expr.operator.lexeme}")
        }
    }

    override fun visitBinary(expr: Expr.Binary): Any {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.PLUS -> left.asInt() + right.asInt()
            TokenType.MINUS -> left.asInt() - right.asInt()
            TokenType.STAR -> left.asInt() * right.asInt()
            TokenType.SLASH -> left.asInt() / right.asInt()

            TokenType.EQUAL_EQUAL -> left == right
            TokenType.NOT_EQUAL -> left != right

            TokenType.LESS -> left.asInt() < right.asInt()
            TokenType.LESS_EQUAL -> left.asInt() <= right.asInt()
            TokenType.GREATER -> left.asInt() > right.asInt()
            TokenType.GREATER_EQUAL -> left.asInt() >= right.asInt()

            else -> throw RuntimeException("Unsupported binary operator: ${expr.operator.lexeme}")
        }
    }

    override fun visitCall(expr: Expr.Call): Any {
        val function = functions[expr.callee.lexeme]
            ?: throw RuntimeException("Undefined function '${expr.callee.lexeme}'.")

        if (expr.arguments.size != function.parameters.size) {
            throw RuntimeException(
                "Function '${function.name.lexeme}' expected ${function.parameters.size} arguments but got ${expr.arguments.size}."
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
            throw RuntimeException("Function '${function.name.lexeme}' did not return a value.")
        } catch (signal: ReturnSignal) {
            signal.value
        } finally {
            environment = callerEnvironment
            insideFunction = callerInsideFunction
        }
    }
}

private class ReturnSignal(
    val value: Any,
) : RuntimeException(null, null, false, false)

private fun Any.asInt(): Int {
    return this as? Int
        ?: throw RuntimeException("Expected integer value, got ${this::class.simpleName}.")
}

private fun Any.asBoolean(): Boolean {
    return this as? Boolean
        ?: throw RuntimeException("Expected boolean value, got ${this::class.simpleName}.")
}