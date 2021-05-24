/* Exemplo de Programa na linguagem L */

int n,i,j,valor;
char s1[21];

main
{
    write("Digite compiladores em letras maiusculas:");
    readln(s1);
    if (s1="COMPILADORES") then writeln("correto"); else writeln("incorreto");
    if ((not (s1="COMPILADORES") and (TRUE= not FALSE) or (1>2)) then writeln(s1," eh diferente de COMPILADORES.");
    if (not ( not( s1="COMPILADORES")) and (TRUE= not FALSE) or (1>2)) then writeln(s1," eh igual a COMPILADORES.");
    write("Agora digite um numero positivo entre 10 e 20: ");
    readln(n);
    valor:=0;
    for (i:=1; i<=n; i:=i+1)
    for (j:=1; j<=n; j:=j+1)
      valor:=valor+(40/20)*((n-0*8)+(7%6)-(i+1-1-i)-n);
  writeln("O quadrado de ",n," eh ", valor);
}
