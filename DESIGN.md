# Design

This project implements an interpreter for a small artificial programming language.

The goal is not to build the smallest possible prototype, but to produce a clean and readable mini-project with explicit design decisions.

## Execution model

The implementation follows a compiler-style front end:

1. source text
2. lexical analysis
3. parsing into AST
4. execution by interpreting the AST

Although the internal structure is split into phases, the final system is still an interpreter: it executes the input program directly rather than compiling it into bytecode or machine code.

## Supported value types

The language supports:

- integers
- booleans

No other value types are planned unless they become necessary.

## Statements

The language is expected to support these statement forms:

- variable assignment
- `if ... then ... else ...`
- `while ... do ...`
- function declaration
- `return`
- statement sequences

Statement sequences are important because the examples use comma-separated execution, for example inside `while` bodies.

## Expressions

The language is expected to support:

- integer literals
- boolean literals
- variable references
- arithmetic operations
- comparison operations
- unary minus
- parenthesized expressions
- function calls

## Functions

Functions are declared by name and stored globally.

The current design choice is:

- function declarations are allowed only at top level
- functions may call other functions, including recursively
- each function call creates its own local scope

## Variable scope

The current scope model is intentionally simple:

- assignments at top level write to global variables
- assignments inside a function write to that function's local scope
- variable lookup inside a function first checks local variables, then global variables

This keeps function execution predictable while still allowing functions to read global values if needed.

## Conditions

Conditions in `if` and `while` must evaluate to boolean values.

There is no implicit conversion between integers and booleans.

## Output

After program execution, the interpreter prints all global variables.

Planned output format:

```text
name: value
```

The current preferred choice is to preserve insertion order of global variables, since this feels natural and matches the examples better than alphabetical sorting.

## Errors

The interpreter should fail explicitly on invalid programs or invalid runtime behavior.

Examples include:

- undefined variable
- undefined function
- wrong number of function arguments
- non-boolean condition
- `return` outside a function

Error handling should prioritize clarity over complexity.

## Parsing strategy

The parser is planned as a hand-written recursive descent parser.

Expressions will use standard precedence handling.

This choice keeps the implementation compact, explicit, and easy to reason about.

## Testing strategy

The development process is test-driven where it matters most:

- sample programs from the assignment will be turned into acceptance tests
- lexer, parser, and interpreter behavior will be tested incrementally
- design notes will be added for major decisions, not for every tiny implementation detail

## Out of scope

The following are intentionally out of scope for now:

- bytecode generation
- virtual machine execution
- static type checking
- closures
- nested function declarations
- advanced parser recovery
