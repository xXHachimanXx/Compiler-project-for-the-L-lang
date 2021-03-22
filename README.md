# Compiler-project-for-the-L-lang

This is a compiler for the L language, an artifical language of the Compilers discipline.

Our generated assembly is compatible with the 8086 processor with registers of 16 bits.

## Running the compiler

Compiling the L language to .asm on Linux:

```
javac Main.java && java Main < exemplo1.l > exemplo1.asm; rm *.class
```

## Transforming the .asm file to an .exe file

To test our .asm files, we used DosBox to emulate a 16 bit operational system like DOS.

Inside DosBox run:

```
mount c: /COMPLETE/PATH/TO/THE/8086/FOLDER/ON/YOUR/COMPUTER
masm /L exemplo1.asm
link exemplo1.obj
exemplo1.exe
```

## Language Grammar

```
PROGRAM ::= ((VAR_DECLS | CONST_DECLS) ';')* 'main' '{' STATEMENT* '}'

STATEMENT ::=
    '{' STATEMENT* '}'
    | ('write' | 'writeln') '(' EXPRESSION (',' EXPRESSION)* ')' ';'
    | 'readln' '(' IDENTIFIER ('[' EXPRESSION ']')? ')' ';'
    | 'if' '(' EXPRESSION ')' 'then' STATEMENT ('else' STATEMENT)?
    | 'for' '(' ASSIGN_STATEMENTS? ';' EXPRESSION ';' ASSIGN_STATEMENTS? ')' STATEMENT
    | ASSIGN_STATEMENTS ';'
    | VAR_DECLS ';'
    | CONST_DECLS ';'

CHARACTER ::= [a-zA-Z0-9_ .,;:(){}[=<>%+*/'"] | ']' | '-'

CHARACTER_CONST ::= "'" CHARACTER "'"

STRING ::= '"' CHARACTER* '"'

INTEGER ::= [0-9]+

BOOLEAN_CONST ::= 'TRUE' | 'FALSE'

HEX_INTEGER ::= '0' [0-9ABCDEF] [0-9ABCDEF] 'h'

IDENTIFIER ::= ( '_' [a-zA-Z0-9_]* [a-zA-Z0-9] | [a-zA-Z] ) [a-zA-Z0-9_]*

ASSIGN_STATEMENT ::= IDENTIFIER ('[' EXPRESSION ']')? ':=' EXPRESSION

ASSIGN_STATEMENTS ::= ASSIGN_STATEMENT (',' ASSIGN_STATEMENT)*

VAR_DECL ::= IDENTIFIER ('[' EXPRESSION ']' | ':=' EXPRESSION)?

VAR_DECLS ::= ('int' | 'char' | 'boolean') VAR_DECL (',' VAR_DECL)*

CONST_DECL ::= IDENTIFIER '=' EXPRESSION

CONST_DECLS ::= 'final' CONST_DECL (',' CONST_DECL)*

PRIMARY_EXPRESSION ::=
    BOOLEAN_CONST
    | CHARACTER_CONST
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

## References

- [Draw railroad diagrams for BNF grammars](https://www.bottlecaps.de/rr/ui)
- [Writing a programming language - the Lexer](https://www.youtube.com/watch?v=TG0qRDrUPpA)
- [Compiler Design: Predictive Parsing-LL(1)](https://www.youtube.com/watch?v=QoOALbef3ZM)
- [Predictive Recursive Descent Parsing](https://www.tutorialspoint.com/compiler_design/compiler_design_top_down_parser.htm)
- [ANSI C Yacc grammar](https://www.lysator.liu.se/c/ANSI-C-grammar-y.html#assignment-expression)
- [Example Language](https://raw.githubusercontent.com/bisqwit/compiler_series/master/ep1/jit-conj-parser1.png)
- [Example Language Video](https://www.youtube.com/watch?v=eF9qWbuQLuw&t=1034s)
- [Compilers Course](https://www.youtube.com/watch?v=8rB8Dvczc1g&list=PL0Z-gyL9saMcajYH26KWKQG0nH2C2fsMQ&index=2)
- [Create a programming language [part 2] - The Lexer](https://www.youtube.com/watch?v=Tfhm0yQ9P8Q)
- [Create a programming language [part 3] - The Parser](https://www.youtube.com/watch?v=4HW3RAoWMpg)
- [Pascal Operator Precedence](https://www.freepascal.org/docs-html/ref/refch12.html)
- [Pascal Grammar in BNF (Backus-Naur Form)](https://condor.depaul.edu/ichu/csc447/notes/wk2/pascal.html)
- [Compilers - Federal University of São Carlos](https://www2.dc.ufscar.br/~mario/ensino/2018s1/compiladores1/)
- [8086 Assembly Language](https://www.youtube.com/watch?v=ThUSyV81tIc&list=PLajZfknhluUSY6weDgx3xYuRsaXUwU8mh)
- [Running MASM](https://users.cs.fiu.edu/~downeyt/cop3402/runmasm.html)
- [8086 assembly interrupt codes](http://www.gabrielececchetti.it/Teaching/CalcolatoriElettronici/Docs/i8086_and_DOS_interrupts.pdf)
