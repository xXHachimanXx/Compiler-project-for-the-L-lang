# Compiler-project-for-the-L-lang

Language Grammar:

```
PROGRAM ::= (VARS_DECL ';')* 'main' STATEMENT

STATEMENT ::=
    '{' STATEMENT* '}'
    | EXPRESSION ';'
    | ('write' | 'writeln') '(' EXPRESSION (',' EXPRESSION)* ')' ';'
    | 'readln' '(' IDENTIFIER ('[' EXPRESSION ']')? ')' ';'
    | 'if' '(' EXPRESSION ')' 'then' STATEMENT ('else' STATEMENT)?
    | 'for' '(' EXPRESSION? ';' EXPRESSION? ';' EXPRESSION? ')' STATEMENT
    | 'return' EXPRESSION ';'
    | VARS_DECL ';'

CHARACTER ::= [a-zA-Z0-9_ .,;:(){}[=<>%+*/'"] | ']' | '-'

STRING ::= '"' CHARACTER* '"'

INTEGER ::= [0-9]+

HEX_INTEGER ::= '0' [0-9ABCDEF] [0-9ABCDEF] 'h'

IDENTIFIER ::= ( '_' [a-zA-Z0-9_]* [a-zA-Z0-9] | [a-zA-Z] ) [a-zA-Z0-9_]*

VAR_DECL ::= IDENTIFIER ((':=' EXPRESSION)? | ('[' (INTEGER | HEX_INTEGER) ']')?)

VARS_DECL ::= ('int' | 'char' | 'boolean' | 'final') VAR_DECL (',' VAR_DECL)*

PRIMARY_EXPRESSION ::=
    'TRUE' | 'FALSE'
    | "'" CHARACTER "'"
    | INTEGER
    | HEX_INTEGER
    | STRING
    | '(' EXPRESSION ')'
    | IDENTIFIER ('[' EXPRESSION ']')?

UNARY_EXPRESSION ::= (('+' | '-' | 'not') PRIMARY_EXPRESSION) | PRIMARY_EXPRESSION

MULTIPLICATIVE_EXPRESSION ::= UNARY_EXPRESSION (('*' | '/' | '%') UNARY_EXPRESSION)*

ADDITIVE_EXPRESSION ::= MULTIPLICATIVE_EXPRESSION (('+' | '-') MULTIPLICATIVE_EXPRESSION)*

RELATIONAL_EXPRESSION ::= ADDITIVE_EXPRESSION (('<' | '>' | '<=' | '>=') ADDITIVE_EXPRESSION)*

EQUALITY_EXPRESSION ::= RELATIONAL_EXPRESSION (('=' | '<>') RELATIONAL_EXPRESSION)*

AND_EXPRESSION ::= EQUALITY_EXPRESSION ('and' EQUALITY_EXPRESSION)*

OR_EXPRESSION ::= AND_EXPRESSION ('or' AND_EXPRESSION)*

ASSIGNMENT_EXPRESSION ::= OR_EXPRESSION (':=' OR_EXPRESSION)?

EXPRESSION ::= ASSIGNMENT_EXPRESSION
```

References:

- [Writing a programming language - the Lexer](https://www.youtube.com/watch?v=TG0qRDrUPpA)
- [Compiler Design: Predictive Parsing-LL(1)](https://www.youtube.com/watch?v=QoOALbef3ZM)
- [Predictive Recursive Descent Parsing](https://www.tutorialspoint.com/compiler_design/compiler_design_top_down_parser.htm)
- [Railroad Diagrams](https://www.bottlecaps.de/rr/ui)
- [C Language Specification](https://www2.cs.arizona.edu/~debray/Teaching/CSc453/DOCS/cminusminusspec.html)
- [Example Language](https://raw.githubusercontent.com/bisqwit/compiler_series/master/ep1/jit-conj-parser1.png)
- [Example Language Video](https://www.youtube.com/watch?v=eF9qWbuQLuw&t=1034s)
- [Compilers Course](https://www.youtube.com/watch?v=8rB8Dvczc1g&list=PL0Z-gyL9saMcajYH26KWKQG0nH2C2fsMQ&index=2)
- [Pascal Operator Precedence](https://montcs.bloomu.edu/Information/operator-precedence.C-Python-Pascal.html#Pascal_ops)
