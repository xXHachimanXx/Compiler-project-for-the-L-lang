.model small
.stack 4000h

; --------------- MACROS

createBoolTemp macro value, tempPtr
    mov ax, value
    mov ds:[tempPtr], al
endm

createCharTemp macro value, tempPtr
    mov ax, value
    mov ds:[tempPtr], al
endm

createIntTemp macro value, tempPtr
    mov ax, value
    mov ds:[tempPtr], ax
endm

getNonIntArrayElement macro arrPtr, subscriptExprPtr, tempPtr
    mov bx, ds:[subscriptExprPtr] ; ax = índice
    add bx, arrPtr ; ax += endereço do arranjo
    mov bl, ds:[bx] ; Pega o elemento do arranjo
    mov ds:[tempPtr], bl ; Coloca na memória temporária
endm

getIntArrayElement macro arrPtr, subscriptExprPtr, tempPtr
    mov bx, ds:[subscriptExprPtr] ; ax = índice
    add bx, bx ; ax = índice * 2
    add bx, arrPtr ; ax += endereço do arranjo
    mov bx, ds:[bx] ; Pega o elemento do arranjo
    mov ds:[tempPtr], bx ; Coloca na memória temporária
endm

negate macro valuePtr, tempPtr
    mov ax, ds:[valuePtr]
    neg ax
    add ax, 1
    mov ds:[tempPtr], al
endm

unaryMinus macro valuePtr, tempPtr
    mov ax, ds:[valuePtr]
    neg ax
    mov ds:[tempPtr], ax
endm

sum macro value1Ptr, value2Ptr, tempPtr
    mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]
    add ax, bx
    mov ds:[tempPtr], ax
endm

subtract macro value1Ptr, value2Ptr, tempPtr
    mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]
    sub ax, bx
    mov ds:[tempPtr], ax
endm

multiply macro value1Ptr, value2Ptr, tempPtr
    mov ax, ds:[value1Ptr]
    cwd ;expandir AX writeln(valor + ((valor / 2 / 2 / (4))) - 3);
    mov bx, ds:[value2Ptr]
    imul bx
    mov ds:[tempPtr], ax
endm

divide macro value1Ptr, value2Ptr, tempPtr
    mov ax, ds:[value1Ptr]
    cwd ;expandir AX
    mov bl, ds:[value2Ptr]
    idiv bx
    mov ds:[tempPtr], ax
endm

module macro value1Ptr, value2Ptr, tempPtr
    mov ax, ds:[value1Ptr]
    cwd ;expandir AX
    mov bx, ds:[value2Ptr]
    idiv bx
    mov ds:[tempPtr], dx
endm

land macro value1Ptr, value2Ptr, tempPtr
    mov al, ds:[value1Ptr]
    and al, ds:[value2Ptr]
    mov ds:[tempPtr], al
endm

lor macro value1Ptr, value2Ptr, tempPtr
    mov al, ds:[value1Ptr]
    or al, ds:[value2Ptr]
    mov ds:[tempPtr], al
endm

; =
relEqualsStr macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotInicio, RotFim, RotVerdadeiro, RotFim2
    mov di, value1Ptr
    mov si, value2Ptr
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

    je RotVerdadeiro
    mov al, 00h
    jmp RotFim2

    RotVerdadeiro:
        mov al, 01h

    RotFim2:
        mov ds:[tempPtr], al
endm

; =
relEquals macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]

    cmp ax, bx

    je RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], al
endm

; =
relEquals1Byte macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov al, ds:[value1Ptr]
    mov bl, ds:[value2Ptr]

    mov ah, 00h
    mov bh, 00h

    cmp ax, bx

    je RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], al
endm

; <>
relNotEquals macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]

    cmp ax, bx

    jne RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], al
endm

; <>
relNotEquals1Byte macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov al, ds:[value1Ptr]
    mov bl, ds:[value2Ptr]

    mov ah, 00h
    mov bh, 00h

    cmp ax, bx

    jne RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], al
endm

; <
relLessThan macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]

    cmp ax, bx

    jl RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], al
endm

; <
relLessThan1Byte macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov al, ds:[value1Ptr]
    mov bl, ds:[value2Ptr]

    mov ah, 00h
    mov bh, 00h

    cmp ax, bx

    jl RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], al
endm

; >
relGreaterThan macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]

    cmp ax, bx

    jg RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], al
endm

; >
relGreaterThan1Byte macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov al, ds:[value1Ptr]
    mov bl, ds:[value2Ptr]

    mov ah, 00h
    mov bh, 00h

    cmp ax, bx

    jg RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], al
endm

; >=
relGreaterThanOrEqualTo macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]

    cmp ax, bx

    jge RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], al
endm

; >=
relGreaterThanOrEqualTo1Byte macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov al, ds:[value1Ptr]
    mov bl, ds:[value2Ptr]

    mov ah, 00h
    mov bh, 00h

    cmp ax, bx

    jge RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], al
endm

; <=
relLessThanOrEqualTo macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]

    cmp ax, bx

    jle RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], al
endm

; <=
relLessThanOrEqualTo1Byte macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov al, ds:[value1Ptr]
    mov bl, ds:[value2Ptr]

    mov ah, 00h
    mov bh, 00h

    cmp ax, bx

    jle RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], al
endm

assignVar macro value1Ptr, value2Ptr
    ; mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]

    mov ds:[value1Ptr], bx
endm

; end_id + index * type
assignArray macro value1Ptr, value2Ptr, idType, idIndexPtr
    ; calcular endereco array
    mov ax, idType
    mov cx, ds:[idIndexPtr]
    mul cx
    add ax, value1Ptr
    ; Usar bx para index de ds
    mov bx, ax
    ; mover valor para ds
    mov ax, ds:[value2Ptr]
    mov ds:[bx], ax
endm

assignStringVar macro idAddr, exprAddr, idSize
    LOCAL RotInicio, RotFim
    mov di, exprAddr ;posição do string
    mov si, idAddr
    mov cl, '$'
    mov ax, 0
    mov dx, idSize

    ; c[2] a 
    ; c = abcde
    RotInicio:
        mov bl, ds:[di] ; tras o caractere do buffer para bl
        mov bh, 0

        cmp bl, cl ;verifica fim string ('$')
        je RotFim

        cmp ax, dx
        je RotFim

        mov ds:[si], bl
        inc di
        inc si
        inc ax

        jmp RotInicio

    RotFim:
        mov ds:[si], cl
endm


print macro ptr
    mov dx, ptr
    mov ah, 09h
    int 21h
endm

; printStr macro idAddr, idSize
;     LOCAL Again

;     mov bx, idAddr ; b = idAddr
;     mov cx, idAddr ; a = idAddr + idSize
;     add cx, idSize ;
; Again:
;     mov dx, bx
;     mov ah, 02h
;     int 21h
;     inc  bx                  ;4.
;     cmp  dx, cx             ;5.
;     jne  Again
; endm

appendDollarToStr macro
    mov dl, '$' ;coloca '$'
    mov ds:[di],dl ;escreve caractere
    add di, 1 ;incrementa base
endm

; strEndPtr - ponteiro para o final da string (onde ficaria o '$')
appendIntToStr macro strEndPtr
    LOCAL R0, R1, R2

    mov di, strEndPtr
    mov cx, 0 ;contador
    cmp ax,0 ;verifica sinal
    jge R0 ;salta se número positivo

    mov bl, 2Dh ;senão, escreve sinal -
    mov ds:[di], bl
    add di, 1 ;incrementa índice
    neg ax ;toma módulo do número

    R0:
        mov bx, 10 ;divisor
    R1:
        add cx, 1 ;incrementa contador
        mov dx, 0 ;estende 32bits p/ div.
        idiv bx ;divide DXAX por BX
        push dx ;empilha valor do resto
        cmp ax, 0 ;verifica se quoc. é 0
        jne R1 ;se não é 0, continua

    ;agora, desemp. os valores e escreve o string
    R2:
        pop dx ;desempilha valor
        add dx, 30h ;transforma em caractere
        mov ds:[di],dl ;escreve caractere
        add di, 1 ;incrementa base
        add cx, -1 ;decrementa contador
        cmp cx, 0 ;verifica pilha vazia
        jne R2 ;se não pilha vazia, loop
        appendDollarToStr
endm

boolToStr macro ptr, strPtr
    LOCAL R0, R1

    mov al, ds:[ptr] ; Traz o booleano da memória
    mov ah, 0 ; Limpa possível lixo em AH
    cmp ax, 0 ; Compara o valor booleano com 0
    je R0

    mov al, '1' ; Se nao for igual a 0
    jmp R1

    R0: ; Se for igual a 0
        mov al, '0'

    R1: ; Se for igual a 1
        mov ds:[strPtr], al ; Coloca na string
endm

intToStr macro ptr, strPtr
    mov ax, ds:[ptr] ; Traz o inteiro para ax
    appendIntToStr strPtr
endm

charToStr macro ptr, strPtr
    mov al, ds:[ptr] ; Traz o caractere para al
    mov ds:[strPtr], al ; Coloca na string
endm

readlnA1 macro globalCounterAddr
    mov dx, globalCounterAddr
    mov al, 0FFh ;ou tam do vetor
    mov ds:[globalCounterAddr], al
    mov ah, 0Ah
    int 21h ; le os caracteres

    mov ah, 02h ; gera a quebra de linha
    mov dl, 0Dh
    int 21h
    mov DL, 0Ah
    int 21h
endm

readlnA2P1 macro globalCounterAddr, idAddr, exprAddr
    LOCAL R0, R1, R2

    mov di, globalCounterAddr ;posição do string
    mov ax, 0 ;acumulador
    mov cx, 10 ;base decimal
    mov dx, 1 ;valor sinal +
    mov bh, 0
    mov bl, ds:[di] ;caractere
    cmp bx, 2Dh ;verifica sinal -
    
    jne R0 ;se não negativo
    mov dx, -1 ;valor sinal -
    add di, 1 ;incrementa posição
    mov bl, ds:[di] ;próximo caractere
    R0:
    push dx ;empilha sinal
    mov dx, 0 ;reg. multiplicação
    
    R1:
    cmp bx, 0dh ;verifica fim string ('\r')
    
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

    mov bx, ds:[exprAddr]
    add bx, bx
    
    add bx, idAddr
    mov ds:[bx], ax

endm

readlnA2P2 macro globalCounterAddr, idAddr, exprAddr
    mov al, ds:[globalCounterAddr] ; tras o caractere do buffer para al
    mov bx, ds:[exprAddr]
    add bx, idAddr
    mov ds:[bx], al
endm

readlnA3P1 macro globalCounterAddr, idAddr
    LOCAL R0, R1, R2

    mov di, globalCounterAddr ;posição do string
    mov ax, 0 ;acumulador
    mov cx, 10 ;base decimal
    mov dx, 1 ;valor sinal +
    mov bh, 0
    mov bl, ds:[di] ;caractere
    cmp bx, 2Dh ;verifica sinal -
    
    jne R0 ;se não negativo
    mov dx, -1 ;valor sinal -
    add di, 1 ;incrementa posição
    mov bl, ds:[di] ;próximo caractere
    R0:
    push dx ;empilha sinal
    mov dx, 0 ;reg. multiplicação
    
    R1:
    cmp bx, 0dh ;verifica fim string ('\r')
    
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

    mov ds:[idAddr], ax
endm

readlnA3P2 macro globalCounterAddr, idAddr
    mov al, ds:[globalCounterAddr] ; tras o caractere do buffer para al
    mov ds:[idAddr], al
endm

readlnA3P3 macro globalCounterAddr, idAddr
    LOCAL RotInicio, RotFim

    mov di, globalCounterAddr ;posição do string
    mov si, idAddr
    
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
        mov bl, '$'
        mov ds:[si], bl
endm

; --------------- DATA SEGMENT

data segment
    db 4000h DUP(64)
    db 13, 10, '$'
    db 255 DUP(?)
    n dw 0
    i dw 0
    j dw 0
    valor dw 0
    s1 db 22 DUP(?)
    db "Digite compiladores em letras maiusculas:", '$'
    db "COMPILADORES", '$'
    db "correto", '$'
    db "incorreto", '$'
    db "COMPILADORES", '$'
    db " eh diferente de COMPILADORES.", '$'
    db "COMPILADORES", '$'
    db " eh igual a COMPILADORES.", '$'
    db "Agora digite um numero positivo entre 10 e 20: ", '$'
    db "O quadrado de ", '$'
    db "-00000", '$'
    db " eh ", '$'
    db "-00000", '$'
data ends

; --------------- CODE

code segment
assume cs:code, ds:data
start:
    MOV AX, data
    MOV DS, AX

    createIntTemp 21, 0
    print 16672
    readlnA1 16387
    readlnA3P3 16389 16650
    relEqualsStr 16650 16714 2
    mov al, ds:[2] ; Traz o booleano da memória
    mov ah, 0 ; Limpa possível lixo em AH
    cmp ax, 0 ; Compara o valor booleano com 0
    ;je R1
    jne R_IF2
    jmp R1
    R_IF2:

    print 16727
    print 16384
    jmp R3
    R1:

    print 16735
    print 16384
    R3:

    relEqualsStr 16650 16745 3
    negate 3 4
    createBoolTemp 1 5
    createBoolTemp 0 6
    negate 6 7
    relEquals1Byte 5 7 8
    land 4 8 9
    createIntTemp 1, 10
    createIntTemp 2, 12
    relGreaterThan 10 12 14
    lor 9 14 15
    mov al, ds:[15] ; Traz o booleano da memória
    mov ah, 0 ; Limpa possível lixo em AH
    cmp ax, 0 ; Compara o valor booleano com 0
    ;je R4
    jne R_IF5
    jmp R4
    R_IF5:

    print 16650
    print 16758
    print 16384
    R4:

    relEqualsStr 16650 16789 16
    negate 16 17
    negate 17 18
    createBoolTemp 1 19
    createBoolTemp 0 20
    negate 20 21
    relEquals1Byte 19 21 22
    land 18 22 23
    createIntTemp 1, 24
    createIntTemp 2, 26
    relGreaterThan 24 26 28
    lor 23 28 29
    mov al, ds:[29] ; Traz o booleano da memória
    mov ah, 0 ; Limpa possível lixo em AH
    cmp ax, 0 ; Compara o valor booleano com 0
    ;je R6
    jne R_IF7
    jmp R6
    R_IF7:

    print 16650
    print 16802
    print 16384
    R6:

    print 16828
    readlnA1 16387
    readlnA3P1 16389 16642
    createIntTemp 0, 30
    assignVar 16648 30
    createIntTemp 1, 32
    assignVar 16644 32
    R8:

    relLessThanOrEqualTo 16644 16642 34
    mov al, ds:[34] ; Traz o booleano da memória
mov ah, 0 ; Limpa possível lixo em AH
cmp ax, 0 ; Compara o valor booleano com 0
;je R11
jne R_FOR12
jmp R11
R_FOR12:
jmp R10
R9:

    createIntTemp 1, 35
    sum 16644 35 37
    assignVar 16644 37
    jmp R8
R10:

    createIntTemp 1, 39
    assignVar 16646 39
    R13:

    relLessThanOrEqualTo 16646 16642 41
    mov al, ds:[41] ; Traz o booleano da memória
mov ah, 0 ; Limpa possível lixo em AH
cmp ax, 0 ; Compara o valor booleano com 0
;je R16
jne R_FOR17
jmp R16
R_FOR17:
jmp R15
R14:

    createIntTemp 1, 42
    sum 16646 42 44
    assignVar 16646 44
    jmp R13
R15:

    createIntTemp 40, 46
    createIntTemp 20, 48
    divide 46 48 50
    createIntTemp 0, 52
    createIntTemp 8, 54
    multiply 52 54 56
    subtract 16642 56 58
    createIntTemp 7, 60
    createIntTemp 6, 62
    module 60 62 64
    sum 58 64 66
    createIntTemp 1, 68
    sum 16644 68 70
    createIntTemp 1, 72
    subtract 70 72 74
    subtract 74 16644 76
    subtract 66 76 78
    subtract 78 16642 80
    multiply 50 80 82
    sum 16648 82 84
    assignVar 16648 84
    jmp R14
R16:

    jmp R9
R11:

    print 16876
    intToStr 16642 16891
    print 16891
    print 16898
    intToStr 16648 16903
    print 16903
    print 16384

    MOV AH, 4CH ; Exit
    INT 21H
code ends
end start

