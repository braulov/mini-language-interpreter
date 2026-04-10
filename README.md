# Artificial Language Interpreter

A small interpreter in Kotlin for the test task language.

The interpreter reads a program from standard input, executes it, and prints the values of all global variables to standard output.

## Build

```bash
./gradlew build
```

## Run

```bash
./gradlew run
```

You can also pipe a program directly:

```bash
echo "x = 2
y = (x + 2) * 2" | ./gradlew run -q
```

## Language overview

The language is intentionally small and supports:

- integer literals
- boolean literals: `true`, `false`
- variables
- arithmetic operators: `+`, `-`, `*`, `/`
- comparison operators: `==`, `!=`, `<`, `<=`, `>`, `>=`
- `if ... then ... else ...`
- `while ... do ...`
- function declarations
- `return`
- function calls
- statement sequences
- brace block bodies for `while`

### Example

```text
fun fact_rec(n) { if n <= 0 then return 1 else return n * fact_rec(n - 1) }
a = fact_rec(5)
```

Output:

```text
a: 120
```

## Syntax and semantics

### Variables

Variables are created by assignment.

At top level, assignments create or update global variables.

Inside a function call, assignments create or update local variables in that function's scope.

Variable lookup inside a function is:

1. local scope
2. global scope

This means functions may read global variables, but local assignments inside functions do not overwrite globals.

### Functions

Functions are declared by name:

```text
fun add(a, b) { return a + b }
result = add(2, 2)
```

Functions:

- are declared at top level
- are called by name
- bind arguments by position
- support recursion
- must return a value if used in an expression

### Statements

Supported statement forms:

- assignment
- `if ... then ... else ...`
- `while ... do ...`
- `fun ...`
- `return`

## Control Flow

`if` supports single-statement and brace-block branches:

```text
if x > 0 then y = 1 else y = 2

if x > 0 then {
    y = 1
    z = 2
} else {
    y = 3
}
```
`while` supports inline comma-separated bodies and brace-block bodies:
```text
while x < 3 do y = y + 1, x = x + 1

while x < 3 do {
    y = y + 1
    x = x + 1
}
```
### Conditions

Conditions in `if` and `while` must evaluate to booleans.

There is no implicit conversion between integers and booleans.

For example, this is invalid:

```text
if 1 then x = 2 else x = 3
```

### Output

After execution, the interpreter prints all global variables in insertion order:

```text
name: value
```

Only global variables are printed. Function-local variables are not printed.

## Errors

The interpreter reports lexer, parser, and runtime errors with source line information where available.

Examples of runtime errors:

- undefined variable
- undefined function
- wrong number of function arguments
- `return` outside a function
- non-boolean condition
- arithmetic on non-integer values
- division by zero
- function call without a return value

## Implementation overview

The project uses a classic handwritten interpreter pipeline:

1. **Lexer** converts source text into tokens.
2. **Parser** uses recursive descent to build an AST.
3. **Interpreter** walks the AST and executes the program.

Expressions and statements are represented as AST nodes, and the interpreter dispatches through visitors.

## Project structure

```text
src/main/kotlin/
├── Main.kt
├── ast/
├── lexer/
├── parser/
└── runtime/

src/test/kotlin/
├── lexer/
├── parser/
└── runtime/
```

## Testing

The project includes:

- lexer tests
- parser tests
- interpreter happy-path tests
- negative parser/runtime tests
- acceptance tests based on the sample scenarios from the task

Run all tests with:

```bash
./gradlew test
```


## Notes

A short architecture summary is available in `ARCHITECTURE.md`.
