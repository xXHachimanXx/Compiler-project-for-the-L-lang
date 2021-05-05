/* Exemplo de Programa na linguagem L */

int n;
int nome[3999];
final MAXITER=10;

main {
/* Programa Principal */
    n:=3;
  write("Digite seu nome: ");
  readln(nome);
  for (n:=1; n<=MAXITER; n:=n+1)
    writeln("Ola' ",nome);
}