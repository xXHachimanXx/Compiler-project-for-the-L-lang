/* Exemplo de Programa na linguagem L */

char nome[34];
int nome1[34];
int n1 := 1;
int n[3], max:=0, i;

main
{
  if (max > n1) then {
      if (n[0] > n[2]) then max:=3; else max:=n[2];
    } else if (2 > n[2]) then max:=n[1]; else max:=n[2];

  for (i:=0; i<2; i:=i+1) {
    write("Digite um numero: ");
    readln(n[i]);
  }
  if ((2 > 3) and (3 < 4)) then {
    if (n[0] > n[2]) then max:=3; else max:=n[2];
  } else if (2 > n[2]) then max:=n[1]; else max:=n[2];
  writeln("O maior numero e' ",max);
}