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
    mov ax, ds:[subscriptExprPtr] ; ax = índice
    add ax, arrPtr ; ax += endereço do arranjo
    mov al, ds:[ax] ; Pega o elemento do arranjo
    mov ds:[tempPtr], al ; Coloca na memória temporária
endm

getIntArrayElement macro arrPtr, subscriptExprPtr, tempPtr
    mov ax, ds:[subscriptExprPtr] ; ax = índice
    add ax, ax ; ax = índice * 2
    add ax, arrPtr ; ax += endereço do arranjo
    mov ax, ds:[ax] ; Pega o elemento do arranjo
    mov ds:[tempPtr], ax ; Coloca na memória temporária
endm

negate macro valuePtr, tempPtr
    mov ax, ds:[valuePtr]
    neg ax
    add ax, 1
    mov ds:[tempPtr], al
endm

minus macro valuePtr, tempPtr
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
    mov ax, ds:[value1Ptr]
    cwd ;expandir AX
    and ax, ds:[value2Ptr]
    mov ds:[tempPtr], ax
endm

print macro ptr
    mov dx, ptr
    mov ah, 09h
    int 21h
endm

appendDollarToStr macro
    mov dx, '$' ;coloca '$'
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

; --------------- DATA SEGMENT

data segment
    db 4000h DUP(64)
    db 13, 10, '$'
    valor1 dw -5
    valor2 dw 10
    valor3 dw 7
    c db '2'
    db "-00000", '$'
    db "-00000", '$'
    db "-00000", '$'
    db "0", '$'
data ends

; --------------- CODE

code segment
assume cs:code, ds:data
start:
    MOV AX, data
    MOV DS, AX

    createIntTemp 5, 0
    createIntTemp 10, 2
    createIntTemp 7, 4
    createCharTemp '2' 6
    createIntTemp 2, 7
    multiply 16387 7 9
    intToStr 9 16394
    print 16394
    print 16384
    createIntTemp 2, 11
    multiply 16389 11 13
    intToStr 13 16401
    print 16401
    print 16384
    createIntTemp 2, 15
    minus 15 17
    multiply 16391 17 19
    intToStr 19 16408
    print 16408
    print 16384
    charToStr 16393 16415
    print 16415
    print 16384

    MOV AH, 4CH ; Exit
    INT 21H
code ends
end start

