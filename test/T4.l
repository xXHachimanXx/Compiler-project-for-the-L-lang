/* Exemplo de Programa na linguagem L */

int n, i, j;
char seq[31];
int ordem[30],k;

main {
/* Programa Principal */

  write("Digite um string de 30 caracteres: ");
  readln(seq);
  for (i:=0; i<30; i:=i+1) ordem[i]:=i;
  for (i:=1; i<30; i:=i+1) {
      k:=ordem[i];
      for(j:=i-1; (j>=0) and (seq[ordem[j]] > seq[k]); j:=j-1) ordem[j+1]:=ordem[j];
      ordem[j+1]:=k;
  }
  writeln("String original: ",seq);
  write("String ordenado: ");
  for (i:=0; i<30; i:=i+1) write(seq[ordem[i]]);
  writeln(" ");
}
