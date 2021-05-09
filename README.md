# Compiler-project-for-the-L-lang

This is a compiler for the L language, an artifical language of the Compilers discipline.

Our generated assembly is compatible with the 8086 processor with registers of 16 bits.

## Running the compiler

Compiling the L language to .asm on Linux:

```
javac -encoding "UTF-8" Main.java && java Main < test/simple.l > 8086/simple.asm; rm *.class
```

## Transforming the .asm file to an .exe file

To test our .asm files, we used DosBox to emulate a 16 bit operational system like DOS.

Inside DosBox run:

```
mount c: /COMPLETE/PATH/TO/THE/8086/FOLDER/ON/YOUR/COMPUTER
c:
masm /L simple.asm
link simple.obj
simple.exe
```

You can refresh the file system running `rescan`.

If you wanna **debug** the .exe file, you can simple run `debug simple.exe`, then, inside the debugger, use `t` to go to next line, `r` to show registers and `d ds:0` to show 128 memory positions from the start of the data segment. More commands can be found on https://docs.microsoft.com/en-us/previous-versions/tn-archive/cc722863(v=technet.10)?redirectedfrom=MSDN.

## Language Grammar

```
PROGRAM ::= ((VAR_DECLS | CONST_DECLS) ';')* 'main' '{' TERMINATED_STATEMENT* '}'

TERMINATED_STATEMENT ::=
    WRITE_STATEMENT ';'
    | WRITELN_STATEMENT ';'
    | READLN_STATEMENT ';'
    | IF_STATEMENT
    | FOR_STATEMENT
    | ASSIGN_STATEMENT ';'

STATEMENT ::=
    WRITE_STATEMENT
    | WRITELN_STATEMENT
    | READLN_STATEMENT
    | IF_STATEMENT
    | FOR_STATEMENT
    | ASSIGN_STATEMENT

WRITE_STATEMENT ::= 'write' '(' EXPRESSION (',' EXPRESSION)* ')'

WRITELN_STATEMENT ::= 'writeln' '(' EXPRESSION (',' EXPRESSION)* ')'

READLN_STATEMENT ::= 'readln' '(' IDENTIFIER ('[' EXPRESSION ']')? ')'

IF_STATEMENT ::= 'if' '(' EXPRESSION ')' 'then' STATEMENT_OR_STATEMENTS ('else' STATEMENT_OR_STATEMENTS)?

FOR_STATEMENT ::= 'for' '(' COMMA_SEPARATED_STATEMENTS? ';' EXPRESSION ';' COMMA_SEPARATED_STATEMENTS? ')' STATEMENT_OR_STATEMENTS

ASSIGN_STATEMENT ::= IDENTIFIER ('[' EXPRESSION ']')? ':=' EXPRESSION

COMMA_SEPARATED_STATEMENTS ::= STATEMENT (',' STATEMENT)*

STATEMENT_OR_STATEMENTS ::= TERMINATED_STATEMENT | '{' TERMINATED_STATEMENT* '}'

CHARACTER ::= [a-zA-Z0-9_ .,;:(){}[=<>%+*/'"] | ']' | '-'

CHARACTER_CONST ::= "'" CHARACTER "'"

STRING ::= '"' CHARACTER* '"'

INTEGER ::= [0-9]+

BOOLEAN_CONST ::= 'TRUE' | 'FALSE'

HEX_INTEGER ::= '0' [0-9ABCDEF] [0-9ABCDEF] 'h'

CONST ::= INTEGER | HEX_INTEGER | CHARACTER_CONST | STRING | BOOLEAN_CONST

IDENTIFIER ::= ( '_' [a-zA-Z0-9_]* [a-zA-Z0-9] | [a-zA-Z] ) [a-zA-Z0-9_]*

VAR_DECL ::= IDENTIFIER ('[' CONST ']' | ':=' CONST)?

VAR_DECLS ::= ('int' | 'char' | 'boolean') VAR_DECL (',' VAR_DECL)*

CONST_DECL ::= IDENTIFIER '=' CONST

CONST_DECLS ::= 'final' CONST_DECL (',' CONST_DECL)*

PRIMARY_EXPRESSION ::=
    CONST
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
- [MASM Debugger](https://docs.microsoft.com/en-us/previous-versions/tn-archive/cc722863(v=technet.10)?redirectedfrom=MSDN)
- [8086 assembly interrupt codes](http://www.gabrielececchetti.it/Teaching/CalcolatoriElettronici/Docs/i8086_and_DOS_interrupts.pdf)
- [8086 assembler directives & macros](https://www.sakshieducation.com/Story.aspx?nid=93723)
- [8086 addressing modes](https://www.ic.unicamp.br/~celio/mc404s2-03/addr_modes/intel_addr.html)
