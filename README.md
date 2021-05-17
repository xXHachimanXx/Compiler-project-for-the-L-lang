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

This grammar can be loaded on https://www.bottlecaps.de/rr/ui.

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

CHAR_CONST ::= "'" CHARACTER "'"

STRING ::= '"' CHARACTER* '"'

INTEGER ::= [0-9]+

BOOLEAN_CONST ::= 'TRUE' | 'FALSE'

HEX_CHAR ::= '0' [0-9ABCDEF] [0-9ABCDEF] 'h'

CONST ::= ('+' | '-')? INTEGER | HEX_CHAR | CHAR_CONST | STRING | BOOLEAN_CONST

IDENTIFIER ::= ( '_' [a-zA-Z0-9_]* [a-zA-Z0-9] | [a-zA-Z] ) [a-zA-Z0-9_]*

VAR_DECL ::= IDENTIFIER ('[' CONST ']' | ':=' CONST)?

VAR_DECLS ::= ('int' | 'char' | 'boolean') VAR_DECL (',' VAR_DECL)*

CONST_DECL ::= IDENTIFIER '=' CONST

CONST_DECLS ::= 'final' CONST_DECL (',' CONST_DECL)*

PRIMARY_EXPRESSION ::=
    CONST
    | '(' EXPRESSION ')'
    | IDENTIFIER ('[' EXPRESSION ']')?

UNARY_EXPRESSION ::= (('not' | '+' | '-') UNARY_EXPRESSION) | PRIMARY_EXPRESSION

MULTIPLICATIVE_EXPRESSION ::= UNARY_EXPRESSION (('*' | '/' | '%' | 'and') UNARY_EXPRESSION)*

ADDITIVE_EXPRESSION ::= MULTIPLICATIVE_EXPRESSION (('+' | '-' | 'or') MULTIPLICATIVE_EXPRESSION)*

RELATIONAL_EXPRESSION ::= ADDITIVE_EXPRESSION (('=' | '<>' | '<' | '>' | '<=' | '>=') ADDITIVE_EXPRESSION)*

EXPRESSION ::= RELATIONAL_EXPRESSION
```

## Language Grammar with Translation Scheme using professor's notation

<pre>
<b>PROGRAM -> {1} { [VAR_DECLS | CONST_DECLS] ';' }* 'main' '{' {TERMINATED_STATEMENT}* '}' {2}</b>
{1} {
    MOV AX, data
    MOV DS, AX
}
{2} {
    MOV AH, 4CH
    INT 21H
}

<b>CONST_DECLS -> 'final' CONST_DECL {',' CONST_DECL}*</b>

<b>CONST_DECL -> IDENTIFIER {1} '=' CONST {2}</b>
{1} {
    se tabela.get(IDENTIFIER.lex) != null entao ERRO
    IDENTIFIER.classe = constante
    IDENTIFIER.tamanho = 0
    IDENTIFIER.endereco = contador_global_endereco
}
{2} {
    IDENTIFIER.tipo = CONST.tipo
    se IDENTIFIER.tipo = inteiro entao {
        contador_global_endereco += 2
        dw CONST.lex   # declare word
    }
    senao se IDENTIFIER.tipo = caractere entao {
        contador_global_endereco += 1
        db CONST.lex   # declare byte
    }
    senao se IDENTIFIER.tipo = booleano entao {
        contador_global_endereco += 1
        se CONST.lex = TRUE entao {
            db 1
        } senao {
            db 0
        }
    }
}

<b>VAR_DECLS -> ('int' | 'char' | 'boolean') {1} VAR_DECL {',' {2} VAR_DECL1}*</b>
{1} {
    tipo = guardar o tipo ('int' | 'char' | 'boolean') numa variavel
    VAR_DECL.tipo = tipo # passa o tipo por parâmetro para VAR_DECL
}
{2} {
    VAR_DECL1.tipo = tipo # passa o tipo por parâmetro para VAR_DECL1
}

<b>VAR_DECL -> IDENTIFIER {1} ( '[' CONST {2} ']' | ':=' CONST {3} | lambda {4} )</b>
{1} {
    se tabela.get(IDENTIFIER.lex) != null entao ERRO
    IDENTIFIER.classe = variavel
    IDENTIFIER.endereco = contador_global_endereco
    IDENTIFIER.tipo = VAR_DECL.tipo
    IDENTIFIER.tamanho = 0
}
{2} {
    IDENTIFIER.tamanho = CONST.lex
    se IDENTIFIER.tipo = inteiro entao {
        contador_global_endereco += IDENTIFIER.tamanho * 2
        dw IDENTIFIER.tamanho DUP(?) # declare word array
    }
    senao {
        contador_global_endereco += IDENTIFIER.tamanho
        db IDENTIFIER.tamanho DUP(?) # declare byte array
    }
}
{3} {
    # TODO: VERIFICAR A SEGUINTE POSSIBILIDADE `char n[5] = "abcd";`
    se IDENTIFIER.tipo != CONST.tipo entao ERRO
    se IDENTIFIER.tipo = inteiro entao {
        contador_global_endereco += 2
        dw CONST.lex # declare word
    }
    senao se IDENTIFIER.tipo = caractere entao {
        contador_global_endereco += 1
        db CONST.lex # declare byte
    }
    senao se IDENTIFIER.tipo = booleano entao {
        contador_global_endereco += 1
        se CONST.lex = TRUE entao {
            db 1
        } senao {
            db 0
        }
    }
}
{4} {
    se IDENTIFIER.tipo = inteiro entao {
        contador_global_endereco += 2
        dw 0 # declare word
    }
    senao {
        contador_global_endereco += 1
        db 0 # declare byte
    }
}

<b>VAR_DECL -> IDENTIFIER {1} ':=' CONST {2}</b>
{1} {
    se tabela.get(IDENTIFIER.lex) = null entao ERRO
    IDENTIFIER.tipo = VAR_DECL.tipo
}
{2} {
    se IDENTIFIER.tipo != CONST.tipo entao ERRO
    se IDENTIFIER.tipo = inteiro entao {
        dw CONST.lex
    } senao se IDENTIFIER.tipo = caractere entao {
        db CONST.lex
    } senao se IDENTIFIER.tipo = booleano entao {
        se CONST.lex = TRUE entao {
            db 1
        } senao {
            db 0
        }
    }
}

<b>VAR_DECL -> IDENTIFIER {1} '[' CONST {2} ']'</b>
{1} {
    se tabela.get(IDENTIFIER.lex) = null entao ERRO
    IDENTIFIER.tipo = VAR_DECL.tipo
}
{2} {
    se CONST.tipo != inteiro entao ERRO
    se IDENTIFIER.tipo = inteiro entao {
        dw CONST.lex DUP(?)
    } senao {
        db CONST.lex DUP(?)
    }
}

<b>TERMINATED_STATEMENT -> WRITE_STATEMENT ';'</b>
<b>TERMINATED_STATEMENT -> WRITELN_STATEMENT ';'</b>
<b>TERMINATED_STATEMENT -> READLN_STATEMENT ';'</b>
<b>TERMINATED_STATEMENT -> IF_STATEMENT</b>
<b>TERMINATED_STATEMENT -> FOR_STATEMENT</b>
<b>TERMINATED_STATEMENT -> ASSIGN_STATEMENT ';'</b>

<b>WRITE_STATEMENT -> 'write' '(' EXPRESSION {',' EXPRESSION1}* ')'</b>

<b>WRITELN_STATEMENT -> 'writeln' '(' EXPRESSION {',' EXPRESSION1}* ')'</b>

<b>READLN_STATEMENT -> 'readln' '(' IDENTIFIER [ '[' EXPRESSION ']' ] ')'</b>

<b>IF_STATEMENT -> 'if' '(' EXPRESSION ')' 'then' STATEMENT_OR_STATEMENTS [ 'else' STATEMENT_OR_STATEMENTS ]</b>

<b>FOR_STATEMENT -> 'for' '(' [COMMA_SEPARATED_STATEMENTS] ';' EXPRESSION ';' [COMMA_SEPARATED_STATEMENTS] ')STATEMENT_OR_STATEMENTS</b>

<b>ASSIGN_STATEMENT -> IDENTIFIER [ '[' EXPRESSION ']' ] ':=' EXPRESSION1</b>

<b>STATEMENT_OR_STATEMENTS -> TERMINATED_STATEMENT</b>
<b>STATEMENT_OR_STATEMENTS -> '{' {TERMINATED_STATEMENT}* '}'</b>

<b>COMMA_SEPARATED_STATEMENTS -> STATEMENT {',' STATEMENT}*</b>

<b>STATEMENT -> WRITE_STATEMENT</b>
<b>STATEMENT -> WRITELN_STATEMENT</b>
<b>STATEMENT -> READLN_STATEMENT</b>
<b>STATEMENT -> IF_STATEMENT</b>
<b>STATEMENT -> FOR_STATEMENT</b>
<b>STATEMENT -> ASSIGN_STATEMENT</b>

<b>EXPRESSION -> RELATIONAL_EXPRESSION</b>

<b>RELATIONAL_EXPRESSION -> ADDITIVE_EXPRESSION {'=' ADDITIVE_EXPRESSION1}*</b>
<b>RELATIONAL_EXPRESSION -> ADDITIVE_EXPRESSION {'<>' ADDITIVE_EXPRESSION1}*</b>
<b>RELATIONAL_EXPRESSION -> ADDITIVE_EXPRESSION {'<' ADDITIVE_EXPRESSION1}*</b>
<b>RELATIONAL_EXPRESSION -> ADDITIVE_EXPRESSION {'>' ADDITIVE_EXPRESSION1}*</b>
<b>RELATIONAL_EXPRESSION -> ADDITIVE_EXPRESSION {'<=' ADDITIVE_EXPRESSION1}*</b>
<b>RELATIONAL_EXPRESSION -> ADDITIVE_EXPRESSION {'>=' ADDITIVE_EXPRESSION1}*</b>

<b>ADDITIVE_EXPRESSION -> MULTIPLICATIVE_EXPRESSION {1} {('+' | '-' | 'or') {2} MULTIPLICATIVE_EXPRESSION1 {3}}*</b>
{1} {
    ADDITIVE_EXPRESSION.tipo = MULTIPLICATIVE_EXPRESSION.tipo
    ADDITIVE_EXPRESSION.end = MULTIPLICATIVE_EXPRESSION.end
}
{2} {
    operador = guardar o operador ('+' | '-' | 'or') numa variavel
}
{3} {
    se operador = '+' entao {
        se ADDITIVE_EXPRESSION.tipo != inteiro ou MULTIPLICATIVE_EXPRESSION1.tipo != inteiro entao ERRO
        mov ax, ds:[ADDITIVE_EXPRESSION.end]
        mov bx, ds:[MULTIPLICATIVE_EXPRESSION1.end]
        add ax, bx
        ADDITIVE_EXPRESSION.end = NovoTemp()
        mov ds:[ADDITIVE_EXPRESSION.end], ax
    }
    senao se operador = '-' entao {
        se ADDITIVE_EXPRESSION.tipo != inteiro ou MULTIPLICATIVE_EXPRESSION1.tipo != inteiro entao ERRO
        mov ax, ds:[ADDITIVE_EXPRESSION.end]
        mov bx, ds:[MULTIPLICATIVE_EXPRESSION1.end]
        sub ax, bx
        ADDITIVE_EXPRESSION.end = NovoTemp()
        mov ds:[ADDITIVE_EXPRESSION.end], ax
    }
    senao se operador = 'or' entao {
        se ADDITIVE_EXPRESSION.tipo != booleano ou MULTIPLICATIVE_EXPRESSION1.tipo != booleano entao ERRO
        mov al, ds:[ADDITIVE_EXPRESSION.end]
        or al, ds:[MULTIPLICATIVE_EXPRESSION1.end]
        MULTIPLICATIVE_EXPRESSION.end = NovoTemp()
        mov ds:[MULTIPLICATIVE_EXPRESSION.end], al
    }
}

<b>MULTIPLICATIVE_EXPRESSION -> UNARY_EXPRESSION {1} {('*' | '/' | '%' | 'and') {2} UNARY_EXPRESSION1 {3}}*</b>
{1} {
    MULTIPLICATIVE_EXPRESSION.tipo = UNARY_EXPRESSION.tipo
    MULTIPLICATIVE_EXPRESSION.end = UNARY_EXPRESSION.end
}
{2} {
    operador = guardar o operador ('*' | '/' | '%' | 'and') numa variavel
}
{3} {
    se operador = '*' entao {
        se MULTIPLICATIVE_EXPRESSION.tipo != inteiro ou UNARY_EXPRESSION1.tipo != inteiro entao ERRO
        mov ax, ds:[MULTIPLICATIVE_EXPRESSION.end]
        cwd
        mov bx, ds:[UNARY_EXPRESSION1.end]
        imul bx
        MULTIPLICATIVE_EXPRESSION.end = NovoTemp()
        mov ds:[MULTIPLICATIVE_EXPRESSION.end], ax
    }
    senao se operador = '/' entao {
        se MULTIPLICATIVE_EXPRESSION.tipo != inteiro ou UNARY_EXPRESSION1.tipo != inteiro entao ERRO
        mov ax, ds:[MULTIPLICATIVE_EXPRESSION.end]
        cwd
        mov bx, ds:[UNARY_EXPRESSION1.end]
        idiv bx
        MULTIPLICATIVE_EXPRESSION.end = NovoTemp()
        mov ds:[MULTIPLICATIVE_EXPRESSION.end], ax
    }
    senao se operador = '%' entao {
        se MULTIPLICATIVE_EXPRESSION.tipo != inteiro ou UNARY_EXPRESSION1.tipo != inteiro entao ERRO
        mov ax, ds:[MULTIPLICATIVE_EXPRESSION.end]
        cwd
        mov bx, ds:[UNARY_EXPRESSION1.end]
        idiv bx
        MULTIPLICATIVE_EXPRESSION.end = NovoTemp()
        mov ds:[MULTIPLICATIVE_EXPRESSION.end], dx
    }
    senao se operador = 'and' entao {
        se MULTIPLICATIVE_EXPRESSION.tipo != booleano ou UNARY_EXPRESSION1.tipo != booleano entao ERRO
        mov al, ds:[MULTIPLICATIVE_EXPRESSION.end]
        and al, ds:[UNARY_EXPRESSION1.end]
        MULTIPLICATIVE_EXPRESSION.end = NovoTemp()
        mov ds:[MULTIPLICATIVE_EXPRESSION.end], al
    }
}

<b>UNARY_EXPRESSION -> 'not' UNARY_EXPRESSION1 {1}</b>
{1} {
    se UNARY_EXPRESSION1.tipo != booleano entao ERRO
    UNARY_EXPRESSION.tipo = UNARY_EXPRESSION1.tipo
    UNARY_EXPRESSION.end = NovoTemp()
    mov ax, ds:[UNARY_EXPRESSION1.end]
    neg ax
    add ax, 1
    mov ds:[UNARY_EXPRESSION.end], al
}

<b>UNARY_EXPRESSION -> '+' UNARY_EXPRESSION1 {1}</b>
{1} {
    se UNARY_EXPRESSION1.tipo != inteiro entao ERRO
    UNARY_EXPRESSION.tipo = UNARY_EXPRESSION1.tipo
    UNARY_EXPRESSION.end = UNARY_EXPRESSION1.end
}

<b>UNARY_EXPRESSION -> '-' UNARY_EXPRESSION1 {1}</b>
{1} {
    se UNARY_EXPRESSION1.tipo != inteiro entao ERRO
    UNARY_EXPRESSION.tipo = UNARY_EXPRESSION1.tipo
    UNARY_EXPRESSION.end = NovoTemp()
    mov ax, ds:[UNARY_EXPRESSION1.end]
    neg ax
    mov ds:[UNARY_EXPRESSION.end], ax
}

<b>UNARY_EXPRESSION -> PRIMARY_EXPRESSION {1}</b>
{1} {
    UNARY_EXPRESSION.tipo = PRIMARY_EXPRESSION.tipo
    UNARY_EXPRESSION.end = PRIMARY_EXPRESSION.end
}

<b>PRIMARY_EXPRESSION -> CONST {1}</b>
{1} {
    PRIMARY_EXPRESSION.tipo = CONST.tipo
    PRIMARY_EXPRESSION.end = NovoTemp()
    mov ax, CONST.lex
    mov DS:[PRIMARY_EXPRESSION.end], ax
}

<b>PRIMARY_EXPRESSION -> '(' EXPRESSION {1} ')'</b>
{1} {
    PRIMARY_EXPRESSION.tipo = EXPRESSION.tipo
    PRIMARY_EXPRESSION.end = EXPRESSION.end
}

<b>PRIMARY_EXPRESSION -> IDENTIFIER {1} [ '[' EXPRESSION {2} ']' ]</b>
{1} {
    PRIMARY_EXPRESSION.tipo = IDENTIFIER.tipo
    PRIMARY_EXPRESSION.end = IDENTIFIER.end
}
{2} {
    se EXPRESSION.tipo != inteiro entao ERRO
    PRIMARY_EXPRESSION.end = NovoTemp()
    mov bx, ds:[EXPRESSION.end]
    se PRIMARY_EXPRESSION.tipo = inteiro entao {
        add bx, bx
    }
    add bx, IDENTIFIER.end
    mov bx, ds:[bx]
    mov ds:[PRIMARY_EXPRESSION.end], bx
}
</pre>

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
- [8086 Logical Instructions](https://microcontrollerslab.com/8086-logical-instructions-assembly-examples/)
