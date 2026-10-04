# Atvd avaliativa Hudson - Backend

**Aluno:** Hudson Ribeiro Barbara Junior

**RA:** 1091392413010

API REST para gerenciar tarefas do dia a dia, implementada em Java Spring Boot com PostgreSQL. O projeto contém o código-fonte, os scripts SQL, os serviços, os testes unitários e os testes de integração.

Não há login, cadastro ou entidade de usuário.

OBS: Professor eu codei e rodei o projeto utilizando o PostgreSQL(BANCO) e Vscode(FERRAMENTA) mas tbm é compativel com eclipse e netBeans.

## O que está incluído

| Requisito da atividade | Implementação |
| --- | --- |
| Java Spring Boot | Aplicação Maven com Spring Boot 3.5.16 e Java 17 |
| PostgreSQL e script do banco | Pasta `database/` e `src/main/resources/schema.sql` |
| Classes de modelo | `Tarefa` e `StatusTarefa` |
| Conexão com o banco | Spring Data JPA, driver PostgreSQL e `application.yml` |
| Serviços de implementação | `TarefaService`, com transações para criação, alteração e exclusão |
| Criar, alterar e deletar tarefas | Endpoints POST, PUT e DELETE |
| Consultar tarefas | Endpoints GET com busca por ID, paginação e filtro de status |
| Testes unitários | `TarefaServiceTest`, com JUnit 5 e Mockito |
| Testes de integração | `TarefaApiIT`, com aplicação Spring completa e PostgreSQL real |

## Compatibilidade com Eclipse e Apache NetBeans

O projeto foi executado e testado pelo Maven no terminal do Visual Studio Code. Ele utiliza Java 17, Spring Boot e a estrutura padrão do Maven, permitindo a importação no Eclipse e no Apache NetBeans sem alterar o código. Utilize uma versão da IDE com suporte a Java 17 e Maven.

| IDE | Como importar |
| --- | --- |
| Eclipse | Acesse `File → Import → Maven → Existing Maven Projects`. O suporte a Maven é fornecido pelo m2e. |
| Apache NetBeans | Acesse `File → Open Project` e selecione a pasta do projeto Maven. |

Nas duas IDEs:

1. Selecione a pasta `lista-tarefas-backend`, que contém o arquivo `pom.xml`.
2. Configure o JDK 17 para o projeto e aguarde a resolução das dependências do Maven.
3. Configure as variáveis de ambiente `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` na execução da aplicação, usando a conexão do seu PostgreSQL.
4. Execute a classe `br.com.todo.TodoApplication` como uma aplicação Java ou execute o objetivo Maven `spring-boot:run`.

As variáveis definidas em um terminal do VS Code precisam ser configuradas também no ambiente de execução da outra IDE. Para testes de integração, configure as variáveis `DB_TEST_URL`, `DB_TEST_USERNAME` e `DB_TEST_PASSWORD`, conforme a seção de testes automatizados.

A compilação e os testes foram verificados pelo Maven e pelo GitHub Actions. A importação e a execução diretamente no Eclipse e no Apache NetBeans ainda não foram testadas.

## Execução rápida com Docker

Requisitos: Docker Desktop ou Docker Engine com Docker Compose v2. Nesta opção, não é necessário instalar Java, Maven ou PostgreSQL na máquina.

1. Extraia o ZIP e abra um terminal dentro de `lista-tarefas-backend`, na pasta que contém o `pom.xml`.
2. Copie o arquivo de exemplo de configuração.

No Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

No Linux ou macOS:

```bash
cp .env.example .env
```

3. Inicie a aplicação e o banco.

```bash
docker compose up --build -d
docker compose logs -f api
```

Após a mensagem de inicialização do Spring Boot, a API estará disponível em `http://localhost:8080/api/tarefas`. Abrir esse endereço no navegador mostra a listagem em JSON. Trata-se de um backend; os comandos de criação e alteração são enviados por Postman ou outro cliente HTTP.

O container PostgreSQL cria o banco `lista_tarefas`. A API aplica o script de estrutura ao iniciar e valida o mapeamento das entidades. Os dados ficam no volume `dados-postgres` e permanecem ao executar `docker compose down`.

Para encerrar:

```bash
docker compose down
```

## Execução com Java e PostgreSQL instalados

Requisitos: JDK 17, PostgreSQL 17 e acesso à internet na primeira execução do Maven Wrapper. O wrapper incluído baixa o Maven 3.9.11; não é necessário instalar o Maven separadamente.

Crie o banco e a tabela usando o `psql`, dentro da pasta do projeto:

```bash
psql -h localhost -U postgres -d postgres -v ON_ERROR_STOP=1 -f database/01-criar-banco.sql
psql -h localhost -U postgres -d lista_tarefas -v ON_ERROR_STOP=1 -f database/02-criar-tabela.sql
```

O primeiro script cria o banco uma única vez e deve ser executado fora de uma transação. Se o banco já existir, execute apenas o segundo script. A estrutura também é aplicada automaticamente pela API; o segundo comando permite conferir o script de forma independente.

No Windows PowerShell, configure a conexão e execute:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/lista_tarefas"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
chmod +x mvnw
export DB_URL=jdbc:postgresql://localhost:5432/lista_tarefas
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
./mvnw spring-boot:run
```

Use a senha e o usuário definidos na instalação do seu PostgreSQL. A aplicação Java lê variáveis de ambiente; o arquivo `.env` é lido pelo Docker Compose, não pelo comando `spring-boot:run`.

Para gerar um JAR executável:

```powershell
.\mvnw.cmd clean package
java -jar target/lista-tarefas-api-1.0.0.jar
```

No Linux/macOS, substitua `.\mvnw.cmd` por `./mvnw`.

## Modelo e regras adotadas

O enunciado define os campos e as operações. Os limites, os valores de status e o formato da API abaixo são escolhas de implementação.

| Campo JSON | Tipo | Regra |
| --- | --- | --- |
| `id` | Inteiro | Gerado pelo PostgreSQL; usado para identificar uma tarefa |
| `nome` | Texto | Obrigatório, até 120 caracteres; espaços nas extremidades são removidos |
| `descricao` | Texto ou null | Opcional, até 2000 caracteres |
| `status` | Texto | `PENDENTE`, `EM_ANDAMENTO` ou `CONCLUIDA` |
| `observacoes` | Texto ou null | Opcional, até 2000 caracteres |
| `dataCriacao` | Data/hora ISO 8601 | Gerada automaticamente; preservada nas alterações |
| `dataAtualizacao` | Data/hora ISO 8601 | Gerada automaticamente quando os dados são alterados |

Na criação, status omitido ou null assume `PENDENTE`. Na atualização, `nome` e `status` são obrigatórios. O PUT substitui todos os campos editáveis; descrição e observações omitidas são limpas. É possível mudar entre os três status, inclusive reabrir uma tarefa concluída.

ID e datas são campos de resposta e não podem ser enviados no corpo das requisições. Campos desconhecidos, status numéricos e JSON inválido são rejeitados. As datas são armazenadas com fuso horário no PostgreSQL e tratadas em UTC pela aplicação. Uma atualização com todos os valores iguais não altera a data de atualização.

## Endpoints

| Método | Caminho | Resultado |
| --- | --- | --- |
| POST | `/api/tarefas` | Cria uma tarefa; retorna 201 e o cabeçalho Location |
| GET | `/api/tarefas` | Lista tarefas; retorna 200 |
| GET | `/api/tarefas/{id}` | Consulta uma tarefa; retorna 200 ou 404 |
| PUT | `/api/tarefas/{id}` | Substitui os campos editáveis; retorna 200 ou 404 |
| DELETE | `/api/tarefas/{id}` | Exclui uma tarefa; retorna 204 sem corpo ou 404 |

Na listagem, `pagina` começa em 0 e tem valor padrão 0. `tamanho` aceita valores de 1 a 100 e tem valor padrão 20. As tarefas aparecem em ordem decrescente de ID. O filtro `status` é opcional.

Exemplo: `GET /api/tarefas?status=PENDENTE&pagina=0&tamanho=10`.

Os dados de entrada devem ser enviados com `Content-Type: application/json`.

### Criar uma tarefa

```json
{
  "nome": "Estudar Java",
  "descricao": "Revisar Spring Boot e JPA",
  "status": "PENDENTE",
  "observacoes": "Fazer exercícios após a leitura"
}
```

Resposta ilustrativa:

```json
{
  "id": 1,
  "nome": "Estudar Java",
  "descricao": "Revisar Spring Boot e JPA",
  "status": "PENDENTE",
  "observacoes": "Fazer exercícios após a leitura",
  "dataCriacao": "2026-09-29T23:00:00Z",
  "dataAtualizacao": "2026-09-29T23:00:00Z"
}
```

### Alterar uma tarefa

Envie o corpo abaixo em `PUT /api/tarefas/1`, substituindo 1 pelo ID retornado na criação:

```json
{
  "nome": "Estudar Java",
  "descricao": "Revisão realizada",
  "status": "CONCLUIDA",
  "observacoes": "Exercícios concluídos"
}
```

### Formato da listagem

```json
{
  "itens": [],
  "pagina": 0,
  "tamanho": 20,
  "totalElementos": 0,
  "totalPaginas": 0
}
```

### Erros tratados

Dados ou parâmetros inválidos retornam 400. Um ID válido, mas inexistente, retorna 404. Violações de integridade do banco retornam 409. Exemplo de validação:

```json
{
  "instante": "2026-09-29T23:00:00Z",
  "status": 400,
  "mensagem": "Dados inválidos.",
  "caminho": "/api/tarefas",
  "campos": {
    "nome": "O nome é obrigatório."
  }
}
```

## Testar a API no Postman

Importe `docs/lista-tarefas.postman_collection.json`. A coleção usa a variável `baseUrl`, inicialmente `http://localhost:8080`, e salva automaticamente o ID da tarefa criada em `tarefaId`.

Execute os itens da pasta CRUD na ordem: criar, listar, consultar, alterar e excluir. As requisições incluem verificações de resposta. A pasta Validações contém exemplos que devem retornar 400 e 404.

## Testes automatizados

### Unitários

Não precisam de PostgreSQL ou Docker:

```powershell
.\mvnw.cmd test
```

Os testes isolam o serviço com Mockito e verificam criação, status padrão, campos opcionais, busca, alterações, exclusão, registros inexistentes, paginação e filtro de status.

### Integração com PostgreSQL real

Os testes integram controller, serviço, repositório, validação e script SQL. Usam um banco exclusivo cujo nome deve terminar em `_test`; esse banco é esvaziado antes de cada caso. Não usam H2.

Para iniciar apenas o banco de teste com Docker:

```bash
docker compose --profile testes up -d --wait banco-teste
```

Com as configurações padrão do `.env.example`, execute no Windows:

```powershell
.\mvnw.cmd -Ppostgres-it verify
```

Ou no Linux/macOS:

```bash
./mvnw -Ppostgres-it verify
```

Esse comando executa os unitários, gera o JAR e executa os testes de integração. O banco de teste padrão é `lista_tarefas_test`, na porta 5433.

Se estiver usando um PostgreSQL já instalado, execute `database/03-criar-banco-teste.sql` conectado ao banco administrativo `postgres`. Depois defina a conexão do banco de teste:

```powershell
$env:DB_TEST_URL = "jdbc:postgresql://localhost:5432/lista_tarefas_test"
$env:DB_TEST_USERNAME = "postgres"
$env:DB_TEST_PASSWORD = "postgres"
.\mvnw.cmd -Ppostgres-it verify
```

No Linux/macOS, use `export` para definir as mesmas variáveis e `./mvnw` para executar.

Os resultados ficam em `target/surefire-reports/` e `target/failsafe-reports/`. O workflow em `.github/workflows/testes.yml` configura PostgreSQL e executa a suíte no GitHub Actions após o envio dos arquivos ao repositório.

## Organização do código

| Pasta | Responsabilidade |
| --- | --- |
| `controller` | Endpoints HTTP e validação de entrada |
| `dto` | Objetos de entrada, resposta e paginação |
| `model` | Entidade JPA e enumeração de status |
| `repository` | Persistência com Spring Data JPA |
| `service` | Operações e transações do CRUD |
| `exception` | Mensagens e tratamento de erros |
| `database` | Scripts de criação do banco e da tabela |
| `src/test` | Testes unitários e de integração |
| `docs` | Coleção Postman e orientações de entrega |

## Referências técnicas

- [Requisitos do Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/)
- [Validação do Spring MVC](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html)
- [Restrições no PostgreSQL 17](https://www.postgresql.org/docs/17/ddl-constraints.html)
- [Maven Wrapper](https://maven.apache.org/wrapper/)
