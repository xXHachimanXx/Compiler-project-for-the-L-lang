/* Exemplo de Programa na linguagem L */

int n;
boolean b;
char c;
int n1[3];
char nome[40];
final MAXITER=10;

main {
/* Programa Principal */
    c:='T';
  write("Digite seu nome: ");
  readln(nome);
  for (n:=1; n<=MAXITER; n:=n+1)
    writeln("Ola' ",nome);
}