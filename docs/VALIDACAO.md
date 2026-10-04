# Validação do projeto

## Resultado

O projeto foi compilado e empacotado com sucesso. A suíte foi executada em Java 17, Maven 3.9.11 e PostgreSQL 17.

| Suíte | Executados | Falhas | Erros | Ignorados |
| --- | ---: | ---: | ---: | ---: |
| Unitários - TarefaServiceTest | 12 | 0 | 0 | 0 |
| Integração - TarefaApiIT | 36 | 0 | 0 | 0 |
| Total | 48 | 0 | 0 | 0 |

Comando para reproduzir a suíte completa, com as variáveis de conexão configuradas:

```bash
./mvnw -Ppostgres-it verify
```

Os testes de integração confirmam o uso de PostgreSQL real. Foram verificadas as operações do CRUD, a persistência dos campos, as datas automáticas, a paginação, o filtro de status, as validações de entrada, as respostas de registros inexistentes e as restrições SQL.

## Conferência da aplicação em execução

O JAR gerado também foi iniciado com conexão PostgreSQL e conferido por requisições HTTP reais:

- GET da lista e consulta de tarefa por ID.
- POST com nome, descrição e observações, status padrão, datas e cabeçalho Location.
- PUT com alteração dos campos e preservação da data de criação.
- GET com filtro de status e paginação.
- DELETE com resposta 204 e corpo vazio.
- Consulta após exclusão com resposta 404.
- Criação com nome inválido com resposta 400 e indicação do campo.

O Maven Wrapper foi executado e confirmou a versão 3.9.11. Os scripts de criação dos bancos foram executados, e o script de estrutura usado pela aplicação corresponde ao arquivo SQL entregue em `database/02-criar-tabela.sql`.

## Como reproduzir

Siga a seção de testes do `README.md`. O banco de integração deve terminar em `_test` e ser separado do banco usado pela aplicação.

A validação acima usa a aplicação Java e PostgreSQL diretamente. A execução via Docker e a execução do workflow na conta GitHub poderão ser conferidas pelos comandos e pelas orientações de entrega incluídos no projeto.
