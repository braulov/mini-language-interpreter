# Design Overview

This project implements an interpreter for a small artificial programming language.

## Architecture

The interpreter uses a compiler-style pipeline:

1. Lexical analysis
2. Parsing into AST
3. AST interpretation

The project intentionally uses explicit intermediate representations rather than direct execution during parsing.

## Design Notes

Detailed design decisions are documented in:

- Language Semantics
- Parser Strategy
- Runtime Model

## Testing

Development proceeds incrementally with acceptance tests and focused unit tests.
