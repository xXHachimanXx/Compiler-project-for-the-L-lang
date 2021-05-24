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

<b>TERMINATED_STATEMENT -> WRITE_STATEMENT ';'</b>
<b>TERMINATED_STATEMENT -> WRITELN_STATEMENT ';'</b>
<b>TERMINATED_STATEMENT -> READLN_STATEMENT ';'</b>
<b>TERMINATED_STATEMENT -> IF_STATEMENT</b>
<b>TERMINATED_STATEMENT -> FOR_STATEMENT</b>
<b>TERMINATED_STATEMENT -> ASSIGN_STATEMENT ';'</b>

<b>WRITE_STATEMENT -> 'write' '(' EXPRESSION {1} {',' EXPRESSION1 {2}}* ')'</b>
{1} {
    se EXPRESSION.tamanho > 0 e EXPRESSION.tipo != string entao ERRO
    se EXPRESSION.tipo = inteiro entao {
        db "-00000", '$' ; Cria uma string na área de dados para 
        mov ax, ds:[EXPRESSION.end] ; Traz o inteiro para ax
        mov di, contador_global_endereco
        contador_global_endereco += 7
        mov cx, 0 ;contador
        cmp ax,0 ;verifica sinal
        R0 := NovoRot
        jge R0 ;salta se número positivo

        mov bl, 2Dh ;senão, escreve sinal -
        mov ds:[di], bl
        add di, 1 ;incrementa índice
        neg ax ;toma módulo do número

        R0:
            mov bx, 10 ;divisor
        R1 := NovoRot
        R1:
            add cx, 1 ;incrementa contador
            mov dx, 0 ;estende 32bits p/ div.
            idiv bx ;divide DXAX por BX
            push dx ;empilha valor do resto
            cmp ax, 0 ;verifica se quoc. é 0
            jne R1 ;se não é 0, continua

        R2 := NovoRot
        ;agora, desemp. os valores e escreve o string
        R2:
            pop dx ;desempilha valor
            add dx, 30h ;transforma em caractere
            mov ds:[di],dl ;escreve caractere
            add di, 1 ;incrementa base
            add cx, -1 ;decrementa contador
            cmp cx, 0 ;verifica pilha vazia
            jne R2 ;se não pilha vazia, loop

            mov dx, '$' ;coloca '$'
            mov ds:[di],dl ;escreve caractere
            add di, 1 ;incrementa base
    }
    senao se EXPRESSION.tipo = caractere entao {
        db "0", '$'

        mov al, ds:[EXPRESSION.end] ; Traz o caractere para al
        mov ds:[contador_global_endereco], al ; Coloca na string
        contador_global_endereco += 2
    }
    senao se EXPRESSION.tipo = booleano entao {
        db "0", '$'

        mov al, ds:[EXPRESSION.end] ; Traz o booleano da memória
        mov ah, 0 ; Limpa possível lixo em AH
        cmp ax, 0 ; Compara o valor booleano com 0
        R0 := NovoRot
        je R0

        mov al, '1' ; Se nao for igual a 0
        R1 := NovoRot
        jmp R1

        R0: ; Se for igual a 0
            mov al, '0'

        R1: ; Se for igual a 1
            mov ds:[contador_global_endereco], al ; Coloca na string
            contador_global_endereco += 2
    }
    senao se EXPRESSION.tipo = string entao {
        mov dx, EXPRESSION.end
        mov ah, 09h
        int 21h
    }
}
{2} {
    se EXPRESSION1.tamanho > 0 e EXPRESSION1.tipo != string entao ERRO
    se EXPRESSION1.tipo = inteiro entao {
        db "-00000", '$'
        mov ax, ds:[EXPRESSION1.end] ; Traz o inteiro para ax
        mov di, contador_global_endereco
        contador_global_endereco += 7
        mov cx, 0 ;contador
        cmp ax,0 ;verifica sinal
        R0 := NovoRot
        jge R0 ;salta se número positivo

        mov bl, 2Dh ;senão, escreve sinal -
        mov ds:[di], bl
        add di, 1 ;incrementa índice
        neg ax ;toma módulo do número

        R0:
            mov bx, 10 ;divisor
        R1 := NovoRot
        R1:
            add cx, 1 ;incrementa contador
            mov dx, 0 ;estende 32bits p/ div.
            idiv bx ;divide DXAX por BX
            push dx ;empilha valor do resto
            cmp ax, 0 ;verifica se quoc. é 0
            jne R1 ;se não é 0, continua

        R2 := NovoRot
        ;agora, desemp. os valores e escreve o string
        R2:
            pop dx ;desempilha valor
            add dx, 30h ;transforma em caractere
            mov ds:[di],dl ;escreve caractere
            add di, 1 ;incrementa base
            add cx, -1 ;decrementa contador
            cmp cx, 0 ;verifica pilha vazia
            jne R2 ;se não pilha vazia, loop

            mov dx, '$' ;coloca '$'
            mov ds:[di],dl ;escreve caractere
            add di, 1 ;incrementa base
    }
    senao se EXPRESSION1.tipo = caractere entao {
        db "0", '$'

        mov al, ds:[EXPRESSION1.end] ; Traz o caractere para al
        mov ds:[contador_global_endereco], al ; Coloca na string
        contador_global_endereco += 2
    }
    senao se EXPRESSION1.tipo = booleano entao {
        db "0", '$'

        mov al, ds:[EXPRESSION1.end] ; Traz o booleano da memória
        mov ah, 0 ; Limpa possível lixo em AH
        cmp ax, 0 ; Compara o valor booleano com 0
        R0 := NovoRot
        je R0

        mov al, '1' ; Se nao for igual a 0
        R1 := NovoRot
        jmp R1

        R0: ; Se for igual a 0
            mov al, '0'

        R1: ; Se for igual a 1
            mov ds:[contador_global_endereco], al ; Coloca na string
            contador_global_endereco += 2
    }
    senao se EXPRESSION1.tipo = string entao {
        mov dx, EXPRESSION1.end
        mov ah, 09h
        int 21h
    }
}

<b>WRITELN_STATEMENT -> 'writeln' '(' EXPRESSION {1} {',' EXPRESSION1 {2}}* ')' {3}</b>
{1} {
    se EXPRESSION.tamanho > 0 e EXPRESSION.tipo != string entao ERRO
    se EXPRESSION.tipo = inteiro entao {
        db "-00000", '$'
        mov ax, ds:[EXPRESSION.end] ; Traz o inteiro para ax
        mov di, contador_global_endereco
        contador_global_endereco += 7
        mov cx, 0 ;contador
        cmp ax,0 ;verifica sinal
        R0 := NovoRot
        jge R0 ;salta se número positivo

        mov bl, 2Dh ;senão, escreve sinal -
        mov ds:[di], bl
        add di, 1 ;incrementa índice
        neg ax ;toma módulo do número

        R0:
            mov bx, 10 ;divisor
        R1 := NovoRot
        R1:
            add cx, 1 ;incrementa contador
            mov dx, 0 ;estende 32bits p/ div.
            idiv bx ;divide DXAX por BX
            push dx ;empilha valor do resto
            cmp ax, 0 ;verifica se quoc. é 0
            jne R1 ;se não é 0, continua

        R2 := NovoRot
        ;agora, desemp. os valores e escreve o string
        R2:
            pop dx ;desempilha valor
            add dx, 30h ;transforma em caractere
            mov ds:[di],dl ;escreve caractere
            add di, 1 ;incrementa base
            add cx, -1 ;decrementa contador
            cmp cx, 0 ;verifica pilha vazia
            jne R2 ;se não pilha vazia, loop

            mov dx, '$' ;coloca '$'
            mov ds:[di],dl ;escreve caractere
            add di, 1 ;incrementa base
    }
    senao se EXPRESSION.tipo = caractere entao {
        db "0", '$'

        mov al, ds:[EXPRESSION.end] ; Traz o caractere para al
        mov ds:[contador_global_endereco], al ; Coloca na string
        contador_global_endereco += 2
    }
    senao se EXPRESSION.tipo = booleano entao {
        db "0", '$'

        mov al, ds:[EXPRESSION.end] ; Traz o booleano da memória
        mov ah, 0 ; Limpa possível lixo em AH
        cmp ax, 0 ; Compara o valor booleano com 0
        R0 := NovoRot
        je R0

        mov al, '1' ; Se nao for igual a 0
        R1 := NovoRot
        jmp R1

        R0: ; Se for igual a 0
            mov al, '0'

        R1: ; Se for igual a 1
            mov ds:[contador_global_endereco], al ; Coloca na string
            contador_global_endereco += 2
    }
    senao se EXPRESSION.tipo = string entao {
        mov dx, EXPRESSION.end
        mov ah, 09h
        int 21h
    }
}
{2} {
    se EXPRESSION1.tamanho > 0 e EXPRESSION1.tipo != string entao ERRO
    se EXPRESSION1.tipo = inteiro entao {
        db "-00000", '$'
        mov ax, ds:[EXPRESSION1.end] ; Traz o inteiro para ax
        mov di, contador_global_endereco
        contador_global_endereco += 7
        mov cx, 0 ;contador
        cmp ax,0 ;verifica sinal
        R0 := NovoRot
        jge R0 ;salta se número positivo

        mov bl, 2Dh ;senão, escreve sinal -
        mov ds:[di], bl
        add di, 1 ;incrementa índice
        neg ax ;toma módulo do número

        R0:
            mov bx, 10 ;divisor
        R1 := NovoRot
        R1:
            add cx, 1 ;incrementa contador
            mov dx, 0 ;estende 32bits p/ div.
            idiv bx ;divide DXAX por BX
            push dx ;empilha valor do resto
            cmp ax, 0 ;verifica se quoc. é 0
            jne R1 ;se não é 0, continua

        R2 := NovoRot
        ;agora, desemp. os valores e escreve o string
        R2:
            pop dx ;desempilha valor
            add dx, 30h ;transforma em caractere
            mov ds:[di],dl ;escreve caractere
            add di, 1 ;incrementa base
            add cx, -1 ;decrementa contador
            cmp cx, 0 ;verifica pilha vazia
            jne R2 ;se não pilha vazia, loop

            mov dx, '$' ;coloca '$'
            mov ds:[di],dl ;escreve caractere
            add di, 1 ;incrementa base
    }
    senao se EXPRESSION1.tipo = caractere entao {
        db "0", '$'

        mov al, ds:[EXPRESSION1.end] ; Traz o caractere para al
        mov ds:[contador_global_endereco], al ; Coloca na string
        contador_global_endereco += 2
    }
    senao se EXPRESSION1.tipo = booleano entao {
        db "0", '$'

        mov al, ds:[EXPRESSION1.end] ; Traz o booleano da memória
        mov ah, 0 ; Limpa possível lixo em AH
        cmp ax, 0 ; Compara o valor booleano com 0
        R0 := NovoRot
        je R0

        mov al, '1' ; Se nao for igual a 0
        R1 := NovoRot
        jmp R1

        R0: ; Se for igual a 0
            mov al, '0'

        R1: ; Se for igual a 1
            mov ds:[contador_global_endereco], al ; Coloca na string
            contador_global_endereco += 2
    }
    senao se EXPRESSION1.tipo = string entao {
        mov dx, EXPRESSION1.end
        mov ah, 09h
        int 21h
    }
}
{3} {
    db 13, 10, '$' ; 13 = '\r', 10 = '\n'
    mov dx, contador_global_endereco
    contador_global_endereco += 3
    mov ah, 09h
    int 21h
}

<b>READLN_STATEMENT -> 'readln' '(' IDENTIFIER {1} ( '[' {2} EXPRESSION {3} ']' | lambda {4} ) ')'</b>
{1} {
    se tabela.get(IDENTIFIER.lex).classe = constante entao ERRO

    db 255 DUP(?) ; cria o buffer

    mov dx, contador_global_endereco
    mov al, 0FFh ;ou tam do vetor
    mov ds:[contador_global_endereco], al
    mov ah, 0Ah
    int 21h ; le os caracteres

    mov ah, 02h ; gera a quebra de linha
    mov dl, 0Dh
    int 21h
    mov DL, 0Ah
    int 21h
}
{2} {
    se tabela.get(IDENTIFIER.lex).tamanho = 0 entao ERRO
}
{3} {
    se EXPRESSION.tipo != inteiro ou EXPRESSION.tamanho > 0 entao ERRO
    se tabela.get(IDENTIFIER.lex).tipo = inteiro entao {
        mov di, contador_global_endereco + 2 ;posição do string
        mov ax, 0 ;acumulador
        mov cx, 10 ;base decimal
        mov dx, 1 ;valor sinal +
        mov bh, 0
        mov bl, ds:[di] ;caractere
        cmp bx, 2Dh ;verifica sinal -
        R0 := NovoRot
        jne R0 ;se não negativo
        mov dx, -1 ;valor sinal -
        add di, 1 ;incrementa posição
        mov bl, ds:[di] ;próximo caractere
        R0:
        push dx ;empilha sinal
        mov dx, 0 ;reg. multiplicação
        R1 := NovoRot
        R1:
        cmp bx, 0dh ;verifica fim string ('\r')
        R2 := NovoRot
        je R2 ;salta se fim string
        imul cx ;mult. 10
        add bx, -48 ;converte caractere para inteiro
        add ax, bx ;soma valor caractere
        add di, 1 ;incrementa posição
        mov bh, 0
        mov bl, ds:[di] ;próximo caractere
        jmp R1 ;loop
        R2:
        pop cx ;desempilha sinal
        imul cx ;mult. sinal

        mov bx, ds:[EXPRESSION.end]
        add bx, bx
        
        add bx, IDENTIFIER.end
        mov ds:[bx], ax
    }
    senao se tabela.get(IDENTIFIER.lex).tipo = caractere entao {
        mov al, ds:[contador_global_endereco + 2] ; tras o caractere do buffer para al
        mov bx, ds:[EXPRESSION.end]
        
        add bx, IDENTIFIER.end
        mov ds:[bx], al
    }
}
{4} {
    se tabela.get(IDENTIFIER.lex).tipo = inteiro entao {
        mov di, contador_global_endereco + 2 ;posição do string
        mov ax, 0 ;acumulador
        mov cx, 10 ;base decimal
        mov dx, 1 ;valor sinal +
        mov bh, 0
        mov bl, ds:[di] ;caractere
        cmp bx, 2Dh ;verifica sinal -
        R0 := NovoRot
        jne R0 ;se não negativo
        mov dx, -1 ;valor sinal -
        add di, 1 ;incrementa posição
        mov bl, ds:[di] ;próximo caractere
        R0:
        push dx ;empilha sinal
        mov dx, 0 ;reg. multiplicação
        R1 := NovoRot
        R1:
        cmp bx, 0dh ;verifica fim string ('\r')
        R2 := NovoRot
        je R2 ;salta se fim string
        imul cx ;mult. 10
        add bx, -48 ;converte caractere para inteiro
        add ax, bx ;soma valor caractere
        add di, 1 ;incrementa posição
        mov bh, 0
        mov bl, ds:[di] ;próximo caractere
        jmp R1 ;loop
        R2:
        pop cx ;desempilha sinal
        imul cx ;mult. sinal

        mov ds:[IDENTIFIER.end], ax
    }
    senao se tabela.get(IDENTIFIER.lex).tipo = caractere entao {
        mov al, ds:[contador_global_endereco + 2] ; tras o caractere do buffer para al
        mov ds:[IDENTIFIER.end], al
    }
    senao se tabela.get(IDENTIFIER.lex).tipo = string entao {
        mov di, contador_global_endereco + 2 ;posição do string
        mov si, IDENTIFIER.end

        RotInicio := NovoRot
        RotFim := NovoRot
        RotInicio:
            mov bl, ds:[di] ; tras o caractere do buffer para bl
            mov bh, 0
            cmp bx, 0dh ;verifica fim string ('\r')
            je RotFim
            mov ds:[si], bl
            inc di
            inc si
            jmp RotInicio

        RotFim:
            mov ds:[si], '$'
    }
}

<b>IF_STATEMENT -> 'if' '(' EXPRESSION {1} ')' 'then' STATEMENT_OR_STATEMENTS ( 'else' {2} STATEMENT_OR_STATEMENTS {3} | lambda {4} )</b>
{1} {
    se EXPRESSION.tipo != booleano ou EXPRESSION.tamanho > 0 entao ERRO
    RotFalso := NovoRot
    mov al, ds:[EXPRESSION.end] ; Traz o booleano da memória
    mov ah, 0 ; Limpa possível lixo em AH
    cmp ax, 0 ; Compara o valor booleano com 0
    je RotFalso
}
{2} {
    RotFim := NovoRot
    jmp RotFim
    RotFalso:
}
{3} {
    RotFim:
}
{4} {
    RotFalso:
}

<b>FOR_STATEMENT -> 'for' '(' [COMMA_SEPARATED_STATEMENTS] ';' {1} EXPRESSION {2} ';' [COMMA_SEPARATED_STATEMENTS] ')' {3} STATEMENT_OR_STATEMENTS {4}</b>
{1} {
    RotInicio := NovoRot
    RotIncremento := NovoRot
    RotComandos := NovoRot
    RotFim := NovoRot
    RotInicio:
}
{2} {
    se EXPRESSION.tipo != booleano ou EXPRESSION.tamanho > 0 entao ERRO
    mov al, ds:[EXPRESSION.end] ; Traz o booleano da memória
    mov ah, 0 ; Limpa possível lixo em AH
    cmp ax, 0 ; Compara o valor booleano com 0
    je RotFim
    jmp RotComandos
    RotIncremento:
}
{3} {
    jmp RotInicio
    RotComandos:
}
{4} {
    jmp RotIncremento
    RotFim:
}

<b>ASSIGN_STATEMENT -> IDENTIFIER {1} ( ':=' EXPRESSION1 {2} | '[' {3} EXPRESSION {4} ']' ':=' EXPRESSION2 {5} )</b>
{1} {
    se tabela.get(IDENTIFIER.lex) = null entao ERRO
}
{2} {
    se tabela.get(IDENTIFIER.lex).tipo != EXPRESSION1.tipo entao ERRO
    se IDENTIFIER.tamanho > 0 e EXPRESSION1.tipo != string entao ERRO
    se IDENTIFIER.tipo = inteiro entao {
        mov ax, ds:[EXPRESSION1.end]
        mov ds:[IDENTIFIER.end], ax
    }
    senao se IDENTIFIER.tipo = caractere entao {
        mov al, ds:[EXPRESSION1.end]
        mov ds:[IDENTIFIER.end], al
    }
    senao se IDENTIFIER.tipo = booleano entao {
        mov al, ds:[EXPRESSION1.end]
        mov ds:[IDENTIFIER.end], al
    }
    senao se IDENTIFIER.tipo = string entao {
        mov di, EXPRESSION1.end ;posição do string
        mov si, IDENTIFIER.end

        RotInicio := NovoRot
        RotFim := NovoRot
        RotInicio:
            mov bl, ds:[di] ; tras o caractere do buffer para bl
            mov bh, 0
            cmp bx, 24h ;verifica fim string ('$')
            je RotFim
            mov ds:[si], bl
            inc di
            inc si
            jmp RotInicio

        RotFim:
            mov ds:[si], '$'
    }
}
{3} {
    se tabela.get(IDENTIFIER.lex).tamanho = 0 entao ERRO
}
{4} {
    se EXPRESSION.tipo != inteiro ou EXPRESSION.tamanho > 0 entao ERRO
}
{5} {
    se tabela.get(IDENTIFIER.lex).tipo != EXPRESSION2.tipo entao ERRO
    se EXPRESSION2.tamanho > 0 entao ERRO
    mov bx, IDENTIFIER.end ; traz o endereço base do arranjo
    mov si, ds:[EXPRESSION.end] ; traz o índice para si
    se tabela.get(IDENTIFIER.lex).tipo = inteiro entao {
        add si, si
    }
    add bx, si
    se IDENTIFIER.tipo = inteiro entao {
        mov ax, ds:[EXPRESSION2.end]
        mov ds:[bx], ax
    }
    senao se IDENTIFIER.tipo = caractere entao {
        mov al, ds:[EXPRESSION2.end]
        mov ds:[bx], al
    }
    senao se IDENTIFIER.tipo = booleano entao {
        mov al, ds:[EXPRESSION2.end]
        mov ds:[bx], al
    }
}

<b>STATEMENT_OR_STATEMENTS -> TERMINATED_STATEMENT</b>
<b>STATEMENT_OR_STATEMENTS -> '{' {TERMINATED_STATEMENT}* '}'</b>

<b>COMMA_SEPARATED_STATEMENTS -> STATEMENT {',' STATEMENT}*</b>

<b>STATEMENT -> WRITE_STATEMENT</b>
<b>STATEMENT -> WRITELN_STATEMENT</b>
<b>STATEMENT -> READLN_STATEMENT</b>
<b>STATEMENT -> IF_STATEMENT</b>
<b>STATEMENT -> FOR_STATEMENT</b>
<b>STATEMENT -> ASSIGN_STATEMENT</b>

<b>EXPRESSION -> RELATIONAL_EXPRESSION {1}</b>
{1} {
    EXPRESSION.tipo = RELATIONAL_EXPRESSION.tipo
    EXPRESSION.end = RELATIONAL_EXPRESSION.end
    EXPRESSION.tamanho = RELATIONAL_EXPRESSION.tamanho
}

<b>RELATIONAL_EXPRESSION -> ADDITIVE_EXPRESSION {1} {('=' | '<>' | '<' | '>' | '<=' | '>=') {2} ADDITIVE_EXPRESSION1 {3}}*</b>
{1} {
    RELATIONAL_EXPRESSION.tipo = ADDITIVE_EXPRESSION.tipo
    RELATIONAL_EXPRESSION.end = ADDITIVE_EXPRESSION.end
    RELATIONAL_EXPRESSION.tamanho = ADDITIVE_EXPRESSION.tamanho
}
{2} {
    operador = guardar o operador ('=' | '<>' | '<' | '>' | '<=' | '>=') numa variavel
}
{3} {
    se operador = '=' e RELATIONAL_EXPRESSION.tamanho > 0 e nao(
        e RELATIONAL_EXPRESSION.tipo = string
        e ADDITIVE_EXPRESSION1.tipo = string
    ) entao ERRO

    se operador = '=' entao {
        se RELATIONAL_EXPRESSION.tipo != ADDITIVE_EXPRESSION1.tipo entao ERRO
        se RELATIONAL_EXPRESSION.tipo = inteiro entao {
            mov ax, ds:[RELATIONAL_EXPRESSION.end]
            mov bx, ds:[ADDITIVE_EXPRESSION1.end]
            cmp ax, bx

            RotVerdadeiro := NovoRot
            je RotVerdadeiro
            mov al, 00h
            RotFim := NovoRot
            jmp RotFim

            RotVerdadeiro:
                mov al, 01h

            RotFim:
                RELATIONAL_EXPRESSION.end = NovoTemp()
                mov ds:[RELATIONAL_EXPRESSION.end], al
        }
        senao se RELATIONAL_EXPRESSION.tipo = caractere entao {
            mov al, ds:[RELATIONAL_EXPRESSION.end]
            mov bl, ds:[ADDITIVE_EXPRESSION1.end]
            mov ah, 0
            mov bh, 0
            cmp ax, bx

            RotVerdadeiro := NovoRot
            je RotVerdadeiro
            mov al, 00h
            RotFim := NovoRot
            jmp RotFim

            RotVerdadeiro:
                mov al, 01h

            RotFim:
                RELATIONAL_EXPRESSION.end = NovoTemp()
                mov ds:[RELATIONAL_EXPRESSION.end], al
        }
        senao se RELATIONAL_EXPRESSION.tipo = booleano entao {
            mov al, ds:[RELATIONAL_EXPRESSION.end]
            mov bl, ds:[ADDITIVE_EXPRESSION1.end]
            mov ah, 0
            mov bh, 0
            cmp ax, bx

            RotVerdadeiro := NovoRot
            je RotVerdadeiro
            mov al, 00h
            RotFim := NovoRot
            jmp RotFim

            RotVerdadeiro:
                mov al, 01h

            RotFim:
                RELATIONAL_EXPRESSION.end = NovoTemp()
                mov ds:[RELATIONAL_EXPRESSION.end], al
        }
        senao se RELATIONAL_EXPRESSION.tipo = string entao {
            mov di, RELATIONAL_EXPRESSION.end
            mov si, ADDITIVE_EXPRESSION1.end
            RotInicio := NovoRot
            RotFim := NovoRot
            RotInicio:
                mov al, ds:[di] ; al = str1[i]
                mov ah, 0
                mov bx, '$'
                cmp ax, bx
                je RotFim ; sai do for caso str1[i] != '$'

                mov al, ds:[di] ; al = str1[i]
                mov ah, 0
                mov bl, ds:[si] ; bl = str2[i]
                mov bh, 0
                cmp ax, bx
                jne RotFim ; break
                inc di
                inc si
                jmp RotInicio
            
            RotFim:
            mov al, ds:[di] ; al = str1[i]
            mov ah, 0
            mov bl, ds:[si] ; bl = str2[i]
            mov bh, 0
            cmp ax, bx

            RotVerdadeiro := NovoRot
            je RotVerdadeiro
            mov al, 00h
            RotFim2 := NovoRot
            jmp RotFim2

            RotVerdadeiro:
                mov al, 01h

            RotFim2:
                RELATIONAL_EXPRESSION.end = NovoTemp()
                mov ds:[RELATIONAL_EXPRESSION.end], al
        }
    }
    senao se operador = '<>' entao {
        se RELATIONAL_EXPRESSION.tipo != ADDITIVE_EXPRESSION1.tipo entao ERRO
        se RELATIONAL_EXPRESSION.tipo = inteiro entao {
            mov ax, ds:[RELATIONAL_EXPRESSION.end]
            mov bx, ds:[ADDITIVE_EXPRESSION1.end]
            cmp ax, bx

            RotVerdadeiro := NovoRot
            je RotVerdadeiro
            mov al, 01h
            RotFim := NovoRot
            jmp RotFim

            RotVerdadeiro:
                mov al, 00h

            RotFim:
                RELATIONAL_EXPRESSION.end = NovoTemp()
                mov ds:[RELATIONAL_EXPRESSION.end], al
        }
        senao se RELATIONAL_EXPRESSION.tipo = caractere entao {
            mov al, ds:[RELATIONAL_EXPRESSION.end]
            mov bl, ds:[ADDITIVE_EXPRESSION1.end]
            mov ah, 0
            mov bh, 0
            cmp ax, bx

            RotVerdadeiro := NovoRot
            je RotVerdadeiro
            mov al, 01h
            RotFim := NovoRot
            jmp RotFim

            RotVerdadeiro:
                mov al, 00h

            RotFim:
                RELATIONAL_EXPRESSION.end = NovoTemp()
                mov ds:[RELATIONAL_EXPRESSION.end], al
        }
        senao se RELATIONAL_EXPRESSION.tipo = booleano entao {
            mov al, ds:[RELATIONAL_EXPRESSION.end]
            mov bl, ds:[ADDITIVE_EXPRESSION1.end]
            mov ah, 0
            mov bh, 0
            cmp ax, bx

            RotVerdadeiro := NovoRot
            je RotVerdadeiro
            mov al, 01h
            RotFim := NovoRot
            jmp RotFim

            RotVerdadeiro:
                mov al, 00h

            RotFim:
                RELATIONAL_EXPRESSION.end = NovoTemp()
                mov ds:[RELATIONAL_EXPRESSION.end], al
        }
    }
    senao se operador = '<' entao {
        se RELATIONAL_EXPRESSION.tipo != inteiro ou ADDITIVE_EXPRESSION1.tipo != inteiro entao ERRO
        mov ax, ds:[RELATIONAL_EXPRESSION.end]
        mov bx, ds:[ADDITIVE_EXPRESSION1.end]
        cmp ax, bx

        RotVerdadeiro := NovoRot
        jl RotVerdadeiro
        mov al, 00h
        RotFim := NovoRot
        jmp RotFim

        RotVerdadeiro:
            mov al, 01h

        RotFim:
            RELATIONAL_EXPRESSION.end = NovoTemp()
            mov ds:[RELATIONAL_EXPRESSION.end], al
    }
    senao se operador = '<=' entao {
        se RELATIONAL_EXPRESSION.tipo != inteiro ou ADDITIVE_EXPRESSION1.tipo != inteiro entao ERRO
        mov ax, ds:[RELATIONAL_EXPRESSION.end]
        mov bx, ds:[ADDITIVE_EXPRESSION1.end]
        cmp ax, bx

        RotVerdadeiro := NovoRot
        jle RotVerdadeiro
        mov al, 00h
        RotFim := NovoRot
        jmp RotFim

        RotVerdadeiro:
            mov al, 01h

        RotFim:
            RELATIONAL_EXPRESSION.end = NovoTemp()
            mov ds:[RELATIONAL_EXPRESSION.end], al
    }
    senao se operador = '>' entao {
        se RELATIONAL_EXPRESSION.tipo != inteiro ou ADDITIVE_EXPRESSION1.tipo != inteiro entao ERRO
        mov ax, ds:[RELATIONAL_EXPRESSION.end]
        mov bx, ds:[ADDITIVE_EXPRESSION1.end]
        cmp ax, bx

        RotVerdadeiro := NovoRot
        jg RotVerdadeiro
        mov al, 00h
        RotFim := NovoRot
        jmp RotFim

        RotVerdadeiro:
            mov al, 01h

        RotFim:
            RELATIONAL_EXPRESSION.end = NovoTemp()
            mov ds:[RELATIONAL_EXPRESSION.end], al
    }
    senao se operador = '>=' entao {
        se RELATIONAL_EXPRESSION.tipo != inteiro ou ADDITIVE_EXPRESSION1.tipo != inteiro entao ERRO
        mov ax, ds:[RELATIONAL_EXPRESSION.end]
        mov bx, ds:[ADDITIVE_EXPRESSION1.end]
        cmp ax, bx

        RotVerdadeiro := NovoRot
        jge RotVerdadeiro
        mov al, 00h
        RotFim := NovoRot
        jmp RotFim

        RotVerdadeiro:
            mov al, 01h

        RotFim:
            RELATIONAL_EXPRESSION.end = NovoTemp()
            mov ds:[RELATIONAL_EXPRESSION.end], al
    }
    RELATIONAL_EXPRESSION.tipo = booleano
}

<b>ADDITIVE_EXPRESSION -> MULTIPLICATIVE_EXPRESSION {1} {('+' | '-' | 'or') {2} MULTIPLICATIVE_EXPRESSION1 {3}}*</b>
{1} {
    ADDITIVE_EXPRESSION.tipo = MULTIPLICATIVE_EXPRESSION.tipo
    ADDITIVE_EXPRESSION.end = MULTIPLICATIVE_EXPRESSION.end
    ADDITIVE_EXPRESSION.tamanho = MULTIPLICATIVE_EXPRESSION.tamanho
}
{2} {
    operador = guardar o operador ('+' | '-' | 'or') numa variavel
}
{3} {
    se ADDITIVE_EXPRESSION.tamanho > 0 entao ERRO
    se MULTIPLICATIVE_EXPRESSION1.tamanho > 0 entao ERRO
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
    MULTIPLICATIVE_EXPRESSION.tamanho = UNARY_EXPRESSION.tamanho
}
{2} {
    operador = guardar o operador ('*' | '/' | '%' | 'and') numa variavel
}
{3} {
    se MULTIPLICATIVE_EXPRESSION.tamanho > 0 entao ERRO
    se UNARY_EXPRESSION1.tamanho > 0 entao ERRO
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
    se UNARY_EXPRESSION1.tamanho > 0 entao ERRO
    UNARY_EXPRESSION.tipo = UNARY_EXPRESSION1.tipo
    UNARY_EXPRESSION.end = NovoTemp()
    UNARY_EXPRESSION.tamanho = UNARY_EXPRESSION1.tamanho
    mov ax, ds:[UNARY_EXPRESSION1.end]
    neg ax
    add ax, 1
    mov ds:[UNARY_EXPRESSION.end], al
}

<b>UNARY_EXPRESSION -> '+' UNARY_EXPRESSION1 {1}</b>
{1} {
    se UNARY_EXPRESSION1.tipo != inteiro entao ERRO
    se UNARY_EXPRESSION1.tamanho > 0 entao ERRO
    UNARY_EXPRESSION.tipo = UNARY_EXPRESSION1.tipo
    UNARY_EXPRESSION.end = UNARY_EXPRESSION1.end
    UNARY_EXPRESSION.tamanho = UNARY_EXPRESSION1.tamanho
}

<b>UNARY_EXPRESSION -> '-' UNARY_EXPRESSION1 {1}</b>
{1} {
    se UNARY_EXPRESSION1.tipo != inteiro entao ERRO
    se UNARY_EXPRESSION1.tamanho > 0 entao ERRO
    UNARY_EXPRESSION.tipo = UNARY_EXPRESSION1.tipo
    UNARY_EXPRESSION.end = NovoTemp()
    UNARY_EXPRESSION.tamanho = UNARY_EXPRESSION1.tamanho
    mov ax, ds:[UNARY_EXPRESSION1.end]
    neg ax
    mov ds:[UNARY_EXPRESSION.end], ax
}

<b>UNARY_EXPRESSION -> PRIMARY_EXPRESSION {1}</b>
{1} {
    UNARY_EXPRESSION.tipo = PRIMARY_EXPRESSION.tipo
    UNARY_EXPRESSION.end = PRIMARY_EXPRESSION.end
    UNARY_EXPRESSION.tamanho = PRIMARY_EXPRESSION.tamanho
}

<b>PRIMARY_EXPRESSION -> CONST {1}</b>
{1} {
    PRIMARY_EXPRESSION.tipo = CONST.tipo
    PRIMARY_EXPRESSION.end = NovoTemp()
    PRIMARY_EXPRESSION.tamanho = 0
    mov ax, CONST.lex
    mov DS:[PRIMARY_EXPRESSION.end], ax
}

<b>PRIMARY_EXPRESSION -> '(' EXPRESSION {1} ')'</b>
{1} {
    PRIMARY_EXPRESSION.tipo = EXPRESSION.tipo
    PRIMARY_EXPRESSION.end = EXPRESSION.end
    PRIMARY_EXPRESSION.tamanho = IDENTIFIER.tamanho
}

<b>PRIMARY_EXPRESSION -> IDENTIFIER {1} ( '[' {2} EXPRESSION {3} ']' | lambda {4} )</b>
{1} {
    se tabela.get(IDENTIFIER.lex) = null entao ERRO
    PRIMARY_EXPRESSION.tipo = IDENTIFIER.tipo
    PRIMARY_EXPRESSION.end = IDENTIFIER.end
}
{2} {
    se tabela.get(IDENTIFIER.lex).tamanho = 0 entao ERRO
    PRIMARY_EXPRESSION.tamanho = 0
}
{3} {
    se EXPRESSION.tipo != inteiro ou EXPRESSION.tamanho > 0 entao ERRO
    PRIMARY_EXPRESSION.end = NovoTemp()
    mov bx, ds:[EXPRESSION.end]
    se PRIMARY_EXPRESSION.tipo = inteiro entao {
        add bx, bx
    }
    add bx, IDENTIFIER.end
    mov bx, ds:[bx]
    mov ds:[PRIMARY_EXPRESSION.end], bx
}
{4} {
    PRIMARY_EXPRESSION.tamanho = IDENTIFIER.tamanho
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
- [8086 Jump out of range](https://stackoverflow.com/questions/39427980/relative-jump-out-of-range-by)
