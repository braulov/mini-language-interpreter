package runtime

import ast.Expr
import ast.Stmt
import lexer.TokenType

class Interpreter {
    private val globals = Environment()
    private val functions = mutableMapOf<String, Stmt.Function>()

    fun interpret(program: List<Stmt>): Map<String, Any> {
        for (statement in program) {
            execute(statement, globals, insideFunction = false)
        }

        return globals.snapshot()
    }

    private fun execute(
        statement: Stmt,
        environment: Environment,
        insideFunction: Boolean,
    ) {
        when (statement) {
            is Stmt.Assignment -> executeAssignment(statement, environment)
            is Stmt.If -> executeIf(statement, environment, insideFunction)
            is Stmt.While -> executeWhile(statement, environment, insideFunction)
            is Stmt.Block -> executeBlock(statement, environment, insideFunction)
            is Stmt.Function -> executeFunctionDeclaration(statement)
            is Stmt.Return -> executeReturn(statement, environment, insideFunction)
        }
    }

    private fun executeAssignment(
        statement: Stmt.Assignment,
        environment: Environment,
    ) {
        val value = evaluate(statement.value, environment)
        environment.assignLocal(statement.name.lexeme, value)
    }

    private fun executeIf(
        statement: Stmt.If,
        environment: Environment,
        insideFunction: Boolean,
    ) {
        val condition = evaluate(statement.condition, environment)

        if (condition.asBoolean()) {
            execute(statement.thenBranch, environment, insideFunction)
        } else {
            execute(statement.elseBranch, environment, insideFunction)
        }
    }

    private fun executeWhile(
        statement: Stmt.While,
        environment: Environment,
        insideFunction: Boolean,
    ) {
        while (evaluate(statement.condition, environment).asBoolean()) {
            execute(statement.body, environment, insideFunction)
        }
    }

    private fun executeBlock(
        statement: Stmt.Block,
        environment: Environment,
        insideFunction: Boolean,
    ) {
        for (nested in statement.statements) {
            execute(nested, environment, insideFunction)
        }
    }

    private fun executeFunctionDeclaration(statement: Stmt.Function) {
        functions[statement.name.lexeme] = statement
    }

    private fun executeReturn(
        statement: Stmt.Return,
        environment: Environment,
        insideFunction: Boolean,
    ) {
        if (!insideFunction) {
            throw RuntimeException("Return is only allowed inside a function.")
        }

        val value = evaluate(statement.value, environment)
        throw ReturnSignal(value)
    }

    private fun evaluate(expression: Expr, environment: Environment): Any {
        return when (expression) {
            is Expr.Literal -> expression.value
                ?: throw RuntimeException("Null literals are not supported.")

            is Expr.Variable -> environment.get(expression.name.lexeme)

            is Expr.Grouping -> evaluate(expression.expression, environment)

            is Expr.Unary -> evaluateUnary(expression, environment)

            is Expr.Binary -> evaluateBinary(expression, environment)

            is Expr.Call -> evaluateCall(expression, environment)
        }
    }

    private fun evaluateUnary(expression: Expr.Unary, environment: Environment): Any {
        val right = evaluate(expression.right, environment)

        return when (expression.operator.type) {
            TokenType.MINUS -> -right.asInt()
            else -> throw RuntimeException("Unsupported unary operator: ${expression.operator.lexeme}")
        }
    }

    private fun evaluateBinary(expression: Expr.Binary, environment: Environment): Any {
        val left = evaluate(expression.left, environment)
        val right = evaluate(expression.right, environment)

        return when (expression.operator.type) {
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

            else -> throw RuntimeException("Unsupported binary operator: ${expression.operator.lexeme}")
        }
    }

    private fun evaluateCall(expression: Expr.Call, callerEnvironment: Environment): Any {
        val function = functions[expression.callee.lexeme]
            ?: throw RuntimeException("Undefined function '${expression.callee.lexeme}'.")

        if (expression.arguments.size != function.parameters.size) {
            throw RuntimeException(
                "Function '${function.name.lexeme}' expected ${function.parameters.size} arguments but got ${expression.arguments.size}."
            )
        }

        val localEnvironment = Environment(globals)

        for ((parameter, argumentExpression) in function.parameters.zip(expression.arguments)) {
            val argumentValue = evaluate(argumentExpression, callerEnvironment)
            localEnvironment.assignLocal(parameter.lexeme, argumentValue)
        }

        return try {
            execute(function.body, localEnvironment, insideFunction = true)
            throw RuntimeException("Function '${function.name.lexeme}' did not return a value.")
        } catch (signal: ReturnSignal) {
            signal.value
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