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
    mov ax, ds:[value1Ptr]
    cwd ;expandir AX
    and ax, ds:[value2Ptr]
    mov ds:[tempPtr], ax
endm

; =
relEquals macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]

    mov ah, 00h
    mov bh, 00h

    cmp ax, bx

    je RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], ax
endm

; <>
relNotEquals macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]

    mov ah, 00h
    mov bh, 00h

    cmp ax, bx

    jne RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], ax
endm

; <
relLessThan macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]

    mov ah, 00h
    mov bh, 00h

    cmp ax, bx

    jl RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], ax
endm

; >
relGreaterThan macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]

    mov ah, 00h
    mov bh, 00h

    cmp ax, bx

    jg RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], ax
endm

; >=
relGreaterThanOrEqualTo macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]

    mov ah, 00h
    mov bh, 00h

    cmp ax, bx

    jge RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], ax
endm

; <=
relLessThanOrEqualTo macro value1Ptr, value2Ptr, tempPtr
    LOCAL RotVerdadeiro, RotFim
    mov ax, ds:[value1Ptr]
    mov bx, ds:[value2Ptr]

    mov ah, 00h
    mov bh, 00h

    cmp ax, bx

    jle RotVerdadeiro
    mov ax, 00h
    jmp RotFim

RotVerdadeiro:
    mov ax, 01h

RotFim:
    mov ds:[tempPtr], ax
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
    LOCAL Again

    ; b = idAddr
    mov bx, idAddr 

    ; a = idAddr + idSize
    mov ax, idAddr 
    add ax, idSize ;

    ; c = exprAddr
    mov si, exprAddr

Again:
    mov  dl, ds:[si]    ; d = str[c]
    mov  ds:[bx], dl    ; C[b] = d
    inc  bx 
    inc  si    
    cmp  bx, ax         
    
    jne  Again

endm


print macro ptr
    mov dx, ptr
    mov ah, 09h
    int 21h
endm

printStr macro idAddr, idSize
    LOCAL Again

    mov bx, idAddr ; b = idAddr
    mov cx, idAddr ; a = idAddr + idSize
    add cx, idSize ;
    

Again:
    mov dx, bx
    mov ah, 02h
    int 21h
    inc  bx                  ;4.
    cmp  dx, cx             ;5.
    jne  Again

    ; for(b = idAddr; b < a; b++, c++) print(b)
; R1:
;     cmp bx, ax
;     jge R2
;     print bx
;     add ax, 1
;     jmp R1
; R2: 
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
