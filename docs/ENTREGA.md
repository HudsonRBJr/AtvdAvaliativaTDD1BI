# Publicar o código e entregar o link

O arquivo ZIP reúne o projeto, sem nomes, RA, assinatura ou identificação de desenvolvedor. A conta usada para publicar o repositório será a conta escolhida no GitHub.

## Publicação pelo site do GitHub

1. Extraia o arquivo ZIP. Abra a pasta `lista-tarefas-backend`.
2. Entre em [github.com](https://github.com) e crie um novo repositório com o nome `lista-tarefas-backend`.
3. Use uma descrição como: `API REST de tarefas em Java Spring Boot com PostgreSQL e testes automatizados.`
4. Escolha a visibilidade adequada para que o professor possa acessar. Se for privado, será necessário conceder acesso ao avaliador.
5. Abra a opção **Add file > Upload files** e envie o conteúdo da pasta do projeto. O `pom.xml` deve ficar na raiz do repositório, ao lado de `README.md`, `src/` e `database/`.
6. Inclua também `.mvn/`, `.github/`, `.gitignore`, `.gitattributes`, `.dockerignore` e `.env.example`. Pastas e arquivos iniciados por ponto podem ficar ocultos no explorador de arquivos; habilite sua exibição antes de selecionar o conteúdo.
7. Não envie `target/` ou seu arquivo `.env` com configurações locais. Envie os arquivos extraídos do projeto, não apenas o ZIP.
8. Confirme o envio dos arquivos. Abra a aba **Actions** e confira o workflow `Testes Java e PostgreSQL`. Ele deve terminar com sucesso.
9. Copie a URL do repositório exibida na barra de endereço. Esse é o link a ser enviado na tarefa do Microsoft Teams.

## Publicação por Git

Como alternativa, crie um repositório vazio no GitHub e abra um terminal dentro da pasta do projeto. Configure o nome e o e-mail de commit de acordo com a conta que será usada, e execute:

```bash
git init
git add .
git commit -m "Implementa backend de lista de tarefas"
git branch -M main
git remote add origin URL_DO_REPOSITORIO
git push -u origin main
```

Substitua `URL_DO_REPOSITORIO` pelo endereço real do repositório criado. O projeto não define identidade de commit nem configura uma conta.

## Conferência antes da entrega

- O `pom.xml`, os fontes Java, o README e os testes estão no repositório.
- A pasta `database/` contém os scripts de criação do banco e da tabela.
- O README explica conexão, execução, endpoints e comandos dos testes.
- O workflow passou ou a suíte foi executada localmente com PostgreSQL.
- O link abre o repositório e o avaliador consegue acessar os arquivos.

Prazo informado no enunciado: **04/10/2026, às 23:59**. A publicação e o envio ao Teams precisam ser feitos na conta de quem está entregando.

Referência: [Enviar arquivos a um repositório - documentação do GitHub](https://docs.github.com/en/repositories/working-with-files/managing-files/adding-a-file-to-a-repository).
