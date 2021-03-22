.model small
.stack 100

print macro msg
    lea dx, msg
    mov ah, 09h
    int 21h
endm

.data
msg1 db "MY FIRST PROGRAM IN 8086!!!!!", '$'

.code
    MOV AX, @DATA
    MOV DS, AX

    ; LOCAL str[20] : BYTE
    print msg1
    ; LEA DX, msg1 ; Load effective address, it's like dx = &msg1
    ; MOV AH, 09H
    ; INT 21H

    MOV AH, 4CH ; Exit
    INT 21H
end
