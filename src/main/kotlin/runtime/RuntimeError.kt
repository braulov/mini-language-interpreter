package runtime

import lexer.Token

class RuntimeError(
    token: Token,
    message: String,
) : RuntimeException("Runtime error at line ${token.line}: $message")