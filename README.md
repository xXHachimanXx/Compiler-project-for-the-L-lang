# Compiler-project-for-the-L-lang

Language Grammar:

```
PROGRAM ::= ((VAR_DECLS | CONST_DECLS) ';')* 'main' '{' STATEMENT* '}'

STATEMENT ::=
    '{' STATEMENT* '}'
    | ('write' | 'writeln') '(' EXPRESSION (',' EXPRESSION)* ')' ';'
    | 'readln' '(' IDENTIFIER ('[' EXPRESSION ']')? ')' ';'
    | 'if' '(' EXPRESSION ')' 'then' STATEMENT ('else' STATEMENT)?
    | 'for' '(' ASSIGN_STATEMENTS? ';' EXPRESSION? ';' ASSIGN_STATEMENTS? ')' STATEMENT
    | 'return' EXPRESSION ';'
    | ASSIGN_STATEMENTS ';'
    | VAR_DECLS ';'
    | CONST_DECLS ';'

CHARACTER ::= [a-zA-Z0-9_ .,;:(){}[=<>%+*/'"] | ']' | '-'

STRING ::= '"' CHARACTER* '"'

INTEGER ::= [0-9]+

HEX_INTEGER ::= '0' [0-9ABCDEF] [0-9ABCDEF] 'h'

IDENTIFIER ::= ( '_' [a-zA-Z0-9_]* [a-zA-Z0-9] | [a-zA-Z] ) [a-zA-Z0-9_]*

ASSIGN_STATEMENT ::= IDENTIFIER ('[' EXPRESSION ']')? ':=' EXPRESSION

ASSIGN_STATEMENTS ::= ASSIGN_STATEMENT (',' ASSIGN_STATEMENT)*

VAR_DECL ::= IDENTIFIER | ASSIGN_STATEMENT | IDENTIFIER '[' EXPRESSION ']'

VAR_DECLS ::= ('int' | 'char' | 'boolean') VAR_DECL (',' VAR_DECL)*

CONST_DECL ::= IDENTIFIER (('=' EXPRESSION) | ('[' EXPRESSION ']'))

CONST_DECLS ::= 'final' CONST_DECL (',' CONST_DECL)*

PRIMARY_EXPRESSION ::=
    'TRUE' | 'FALSE'
    | "'" CHARACTER "'"
    | INTEGER
    | HEX_INTEGER
    | STRING
    | '(' EXPRESSION ')'
    | IDENTIFIER ('[' EXPRESSION ']')?

UNARY_EXPRESSION ::= ('not' UNARY_EXPRESSION) | PRIMARY_EXPRESSION

MULTIPLICATIVE_EXPRESSION ::= UNARY_EXPRESSION (('*' | '/' | '%' | 'and') UNARY_EXPRESSION)*

ADDITIVE_EXPRESSION ::= ('+' | '-')? MULTIPLICATIVE_EXPRESSION (('+' | '-' | 'or') MULTIPLICATIVE_EXPRESSION)*

RELATIONAL_EXPRESSION ::= ADDITIVE_EXPRESSION (('=' | '<>' | '<' | '>' | '<=' | '>=') ADDITIVE_EXPRESSION)*

EXPRESSION ::= RELATIONAL_EXPRESSION
```

References:

- [Draw railroad diagrams for grammars](https://www.bottlecaps.de/rr/ui)
- [Writing a programming language - the Lexer](https://www.youtube.com/watch?v=TG0qRDrUPpA)
- [Compiler Design: Predictive Parsing-LL(1)](https://www.youtube.com/watch?v=QoOALbef3ZM)
- [Predictive Recursive Descent Parsing](https://www.tutorialspoint.com/compiler_design/compiler_design_top_down_parser.htm)
- [Railroad Diagrams](https://www.bottlecaps.de/rr/ui)
- [ANSI C Yacc grammar](https://www.lysator.liu.se/c/ANSI-C-grammar-y.html#assignment-expression)
- [Example Language](https://raw.githubusercontent.com/bisqwit/compiler_series/master/ep1/jit-conj-parser1.png)
- [Example Language Video](https://www.youtube.com/watch?v=eF9qWbuQLuw&t=1034s)
- [Compilers Course](https://www.youtube.com/watch?v=8rB8Dvczc1g&list=PL0Z-gyL9saMcajYH26KWKQG0nH2C2fsMQ&index=2)
- [Pascal Operator Precedence](https://www.freepascal.org/docs-html/ref/refch12.html)
- [Pascal Grammar in BNF (Backus-Naur Form)](https://condor.depaul.edu/ichu/csc447/notes/wk2/pascal.html)
