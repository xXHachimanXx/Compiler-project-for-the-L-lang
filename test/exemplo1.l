/* Exemplo de Programa na linguagem L */

int max := 0;
int n[3], i;

main
{
  for (i:=0; i<3; i:=i+1) {
    write("Digite um numero: ");
    readln(n[i]);
  }
  if (n[0] > n[1]) then {
    if (n[0] > n[2]) then max:=3; else max:=n[2];
  } else if (n[1] > n[2]) then max:=n[1]; else max:=n[2]; 
  writeln("O maior numero e' ",max);
}  