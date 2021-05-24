/* Exemplo de Programa na linguagem L */

int n[4], max, min, i;

main
{
  for (i:=0; i<4; i:=i+1) {
    write("Digite um numero positivo: ");
    readln(n[i]);
  }
  min:=n[0];
  for (i:=1; i<4; i:=i+1)
      if (n[i] > max) then max:=n[i]; else if (n[i] < min) then min:=n[1];
  writeln("O menor numero e' ",min);
  writeln("O maior numero e' ",max);
}  
