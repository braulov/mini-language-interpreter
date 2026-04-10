package runtime

import ast.Expr
import ast.Stmt
import lexer.TokenType

class Interpreter {
    private val globals = Environment()

    fun interpret(program: List<Stmt>): Map<String, Any> {
        for (statement in program) {
            execute(statement)
        }

        return globals.snapshot()
    }

    private fun execute(statement: Stmt) {
        when (statement) {
            is Stmt.Assignment -> executeAssignment(statement)
            is Stmt.If -> executeIf(statement)
            is Stmt.While -> executeWhile(statement)
            is Stmt.Block -> executeBlock(statement)

            is Stmt.Function,
            is Stmt.Return -> {
                throw UnsupportedOperationException(
                    "Statement type ${statement::class.simpleName} is not implemented yet."
                )
            }
        }
    }

    private fun executeAssignment(statement: Stmt.Assignment) {
        val value = evaluate(statement.value)
        globals.assign(statement.name.lexeme, value)
    }

    private fun executeIf(statement: Stmt.If) {
        val condition = evaluate(statement.condition)

        if (condition.asBoolean()) {
            execute(statement.thenBranch)
        } else {
            execute(statement.elseBranch)
        }
    }

    private fun executeWhile(statement: Stmt.While) {
        while (evaluate(statement.condition).asBoolean()) {
            execute(statement.body)
        }
    }

    private fun executeBlock(statement: Stmt.Block) {
        for (nested in statement.statements) {
            execute(nested)
        }
    }

    private fun evaluate(expression: Expr): Any {
        return when (expression) {
            is Expr.Literal -> expression.value
                ?: throw RuntimeException("Null literals are not supported.")

            is Expr.Variable -> globals.get(expression.name.lexeme)

            is Expr.Grouping -> evaluate(expression.expression)

            is Expr.Unary -> evaluateUnary(expression)

            is Expr.Binary -> evaluateBinary(expression)

            is Expr.Call -> {
                throw UnsupportedOperationException("Function calls are not implemented yet.")
            }
        }
    }

    private fun evaluateUnary(expression: Expr.Unary): Any {
        val right = evaluate(expression.right)

        return when (expression.operator.type) {
            TokenType.MINUS -> -right.asInt()
            else -> throw RuntimeException("Unsupported unary operator: ${expression.operator.lexeme}")
        }
    }

    private fun evaluateBinary(expression: Expr.Binary): Any {
        val left = evaluate(expression.left)
        val right = evaluate(expression.right)

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
}

private fun Any.asInt(): Int {
    return this as? Int
        ?: throw RuntimeException("Expected integer value, got ${this::class.simpleName}.")
}

private fun Any.asBoolean(): Boolean {
    return this as? Boolean
        ?: throw RuntimeException("Expected boolean value, got ${this::class.simpleName}.")
}