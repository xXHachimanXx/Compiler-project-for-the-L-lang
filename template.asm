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
%s
data ends

; --------------- CODE

code segment
assume cs:code, ds:data
start:
    MOV AX, data
    MOV DS, AX

%s

    MOV AH, 4CH ; Exit
    INT 21H
code ends
end start