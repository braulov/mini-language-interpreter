# Language Semantics

This note fixes the intended semantics of the language before the lexer, parser, and runtime are implemented.

The language is intentionally small. The goal is to keep the semantics explicit and easy to test rather than to support many features.

## Values

The language supports exactly two value kinds:

- integers
- booleans

No implicit conversion is performed between them.

## Variables

Variables are created by assignment.

At top level, assignments create or update global variables.

Inside a function call, assignments create or update local variables in that function's scope.

## Scope Rules

Function calls execute in their own local scope.

Variable lookup inside a function follows this order:

1. local scope
2. global scope

This means functions may read global variables, but assignments inside functions do not mutate global variables unless such behavior is introduced explicitly in a future design change.

## Functions

Functions are declared by name and stored in a global function registry.

Current design choices:

- function declarations are allowed only at top level
- functions may call themselves recursively
- mutual recursion is also allowed
- function arguments are bound by position
- calling a function with the wrong number of arguments is a runtime error

Functions are not values. They cannot be assigned to variables, returned from other functions, or passed as arguments.

## Statements

The language supports the following statement forms:

- assignment
- `if ... then ... else ...`
- `while ... do ...`
- function declaration
- `return`
- statement sequences

A statement sequence represents multiple statements executed from left to right.

The examples in the task use commas for sequencing, so comma-separated statements are part of the intended language design.

## Expressions

The language supports:

- integer literals
- boolean literals
- variable references
- unary minus
- arithmetic operations
- comparison operations
- parenthesized expressions
- function calls

Arithmetic operators produce integer values.

Comparison operators produce boolean values.

## Conditions

The condition of `if` and `while` must evaluate to a boolean.

Using an integer where a boolean is required is a runtime error.

This rule is intentionally strict in order to keep the language semantics predictable.

## Return Semantics

`return expr` immediately terminates the current function call and produces the value of `expr`.

Using `return` outside a function is invalid and should produce an error.

## Program Output

After execution finishes, the interpreter prints all global variables.

Output format:

```text
name: value
```

The current choice is to preserve insertion order of global variables.

Only global variables are printed. Function-local variables are never printed.

## Evaluation Order

Expression evaluation is left to right wherever it is observable.

Function arguments are evaluated before the function body starts executing.

Statement sequences execute from left to right.

## Errors

The interpreter should fail explicitly for invalid programs or invalid runtime behavior.

Examples include:

- undefined variable
- undefined function
- wrong number of function arguments
- non-boolean condition
- `return` outside a function

At this stage, clarity of behavior is more important than advanced recovery or diagnostics.
