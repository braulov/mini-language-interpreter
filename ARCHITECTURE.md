# Architecture Notes

## Pipeline

The interpreter uses a classic handwritten pipeline:

1. Lexer converts source text into tokens.
2. Parser uses recursive descent to build an AST.
3. Interpreter walks the AST and executes the program.

## Parsing strategy

The parser is handwritten recursive descent.

Expression parsing follows precedence levels:

equality -> comparison -> term -> factor -> unary -> call -> primary

This keeps the grammar simple and explicit for the small language size.

## AST design

Expressions and statements are represented as sealed AST hierarchies.

The interpreter dispatches through the visitor pattern rather than large type-switches.

This keeps node-specific execution logic localized and makes the AST easier to extend.

## Runtime model

Execution uses two environments:

- global environment for top-level variables
- local function environment for function calls

Function-local assignments do not overwrite globals.

Variable lookup inside functions checks:

1. local scope
2. global scope

## Functions

Functions:

- are stored in a global registry by name
- support recursion
- are not first-class values
- do not support closures

`return` is implemented via an internal control-flow exception (`ReturnSignal`)
to unwind nested interpreter frames.

## Error handling

The interpreter uses dedicated lexer, parser, and runtime exceptions.

Errors include source line information where available.

## Intentional simplifications

The language intentionally does not support:

- closures
- nested function declarations
- first-class functions
- static type checking
- advanced parser recovery