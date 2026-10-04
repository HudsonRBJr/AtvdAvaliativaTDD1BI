package br.com.todo.integration;

import br.com.todo.repository.TarefaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TarefaApiIT {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private TarefaRepository repository;

    @BeforeAll
    void exigirPostgreSqlEBancoExclusivoDeTeste() {
        assertThat(jdbc.queryForObject("SELECT version()", String.class)).contains("PostgreSQL");
        String banco = jdbc.queryForObject("SELECT current_database()", String.class);
        if (banco == null || !banco.endsWith("_test")) {
            throw new IllegalStateException("Use um banco exclusivo cujo nome termine em _test.");
        }
    }

    @BeforeEach
    void limparSomenteOBancoDeTeste() {
        jdbc.execute("TRUNCATE TABLE tarefas RESTART IDENTITY");
    }

    @Test
    void criarPersisteTodosOsCamposERetornaLocationEDatas() throws Exception {
        mvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"  Estudar Java  ","descricao":"Revisar JPA","status":"EM_ANDAMENTO",
                 "observacoes":"Fazer exercícios"}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/tarefas/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Estudar Java"))
                .andExpect(jsonPath("$.descricao").value("Revisar JPA"))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.observacoes").value("Fazer exercícios"))
                .andExpect(jsonPath("$.dataCriacao").isString())
                .andExpect(jsonPath("$.dataAtualizacao").isString());

        var tarefa = repository.findById(1L).orElseThrow();
        assertThat(tarefa.getStatus().name()).isEqualTo("EM_ANDAMENTO");
        assertThat(tarefa.getDataAtualizacao()).isEqualTo(tarefa.getDataCriacao());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void criarSomenteComNomeUsaStatusPendente() throws Exception {
        JsonNode tarefa = criar("Estudar", null);
        assertThat(tarefa.path("status").asText()).isEqualTo("PENDENTE");
        assertThat(tarefa.path("descricao").isNull()).isTrue();
        assertThat(tarefa.path("observacoes").isNull()).isTrue();
    }

    @Test
    void buscarPorIdRetornaATarefaPersistida() throws Exception {
        long id = criar("Ler documentação", "CONCLUIDA").path("id").asLong();
        mvc.perform(get("/api/tarefas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ler documentação"))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    @Test
    void atualizarConservaIdECriacaoEModificaDataAtualizacao() throws Exception {
        JsonNode original = criar("Estudar", null);
        long id = original.path("id").asLong();
        MvcResult result = mvc.perform(put("/api/tarefas/{id}", id)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Concluir revisão","descricao":"JPA revisado","status":"CONCLUIDA",
                 "observacoes":"Exercícios feitos"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Concluir revisão"))
                .andExpect(jsonPath("$.descricao").value("JPA revisado"))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.observacoes").value("Exercícios feitos"))
                .andReturn();
        JsonNode atualizada = json(result);
        assertThat(atualizada.path("dataCriacao").asText())
                .isEqualTo(original.path("dataCriacao").asText());
        assertThat(OffsetDateTime.parse(atualizada.path("dataAtualizacao").asText()))
                .isAfter(OffsetDateTime.parse(original.path("dataAtualizacao").asText()));
        var persistida = repository.findById(id).orElseThrow();
        assertThat(persistida.getNome()).isEqualTo("Concluir revisão");
        assertThat(persistida.getDataCriacao().toInstant())
                .isEqualTo(OffsetDateTime.parse(original.path("dataCriacao").asText()).toInstant());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void atualizarLimpaDescricaoEObservacoesOmitidas() throws Exception {
        MvcResult result = mvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Estudar","descricao":"Antes","observacoes":"Antes"}
                        """))
                .andExpect(status().isCreated()).andReturn();
        long id = json(result).path("id").asLong();
        mvc.perform(put("/api/tarefas/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Estudar","status":"PENDENTE"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").isEmpty())
                .andExpect(jsonPath("$.observacoes").isEmpty());
        assertThat(repository.findById(id).orElseThrow().getDescricao()).isNull();
    }

    @Test
    void excluirRemoveRegistroERetorna204SemCorpo() throws Exception {
        long id = criar("Excluir esta tarefa", null).path("id").asLong();
        mvc.perform(delete("/api/tarefas/{id}", id))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        assertThat(repository.existsById(id)).isFalse();
        mvc.perform(get("/api/tarefas/{id}", id)).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void operacaoComTarefaInexistenteRetorna404(String metodo) throws Exception {
        var requisicao = request(HttpMethod.valueOf(metodo), "/api/tarefas/999");
        if (metodo.equals("PUT")) {
            requisicao.contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nome\":\"Atualizada\",\"status\":\"PENDENTE\"}");
        }
        mvc.perform(requisicao).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Tarefa 999 não encontrada."));
        assertThat(repository.count()).isZero();
    }

    @Test
    void listarSemRegistrosRetornaPaginaVazia() throws Exception {
        mvc.perform(get("/api/tarefas")).andExpect(status().isOk())
                .andExpect(jsonPath("$.itens", hasSize(0)))
                .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    void listarPaginaEmOrdemDeIdDecrescente() throws Exception {
        criar("Primeira", null);
        criar("Segunda", null);
        criar("Terceira", null);
        mvc.perform(get("/api/tarefas").param("pagina", "0").param("tamanho", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens", hasSize(2)))
                .andExpect(jsonPath("$.itens[0].nome").value("Terceira"))
                .andExpect(jsonPath("$.itens[1].nome").value("Segunda"))
                .andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2));
        mvc.perform(get("/api/tarefas").param("pagina", "1").param("tamanho", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].nome").value("Primeira"));
    }

    @Test
    void listarFiltraPorStatus() throws Exception {
        criar("Pendente", "PENDENTE");
        criar("Finalizada", "CONCLUIDA");
        criar("Em execução", "EM_ANDAMENTO");
        mvc.perform(get("/api/tarefas").param("status", "CONCLUIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].nome").value("Finalizada"))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @ParameterizedTest
    @MethodSource("corposInvalidos")
    void criarRejeitaDadosInvalidosSemPersistir(String corpo) throws Exception {
        mvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        assertThat(repository.count()).isZero();
    }

    static Stream<String> corposInvalidos() {
        return Stream.of(
                "{}",
                "{\"nome\":null}",
                "{\"nome\":\"   \"}",
                "{\"nome\":\"" + "a".repeat(121) + "\"}",
                "{\"nome\":\"Teste\",\"descricao\":\"" + "a".repeat(2001) + "\"}",
                "{\"nome\":\"Teste\",\"observacoes\":\"" + "a".repeat(2001) + "\"}",
                "{\"nome\":\"Teste\",\"status\":\"INVALIDO\"}",
                "{\"nome\":\"Teste\",\"status\":1}",
                "{\"nome\":\"Teste\",\"dataCriacao\":\"2000-01-01T00:00:00Z\"}",
                "{\"nome\":\"Teste\",\"id\":99}",
                "{malformado}");
    }

    @Test
    void errosDeValidacaoInformamOCampo() throws Exception {
        mvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value("O nome é obrigatório."))
                .andExpect(jsonPath("$.caminho").value("/api/tarefas"));
    }

    @Test
    void atualizarSemStatusRetorna400EConservaRegistro() throws Exception {
        long id = criar("Original", "CONCLUIDA").path("id").asLong();
        mvc.perform(put("/api/tarefas/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Alterada\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.status").isString());
        assertThat(repository.findById(id).orElseThrow().getNome()).isEqualTo("Original");
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/tarefas/0", "/api/tarefas/-1", "/api/tarefas/abc",
            "/api/tarefas?pagina=-1", "/api/tarefas?tamanho=0", "/api/tarefas?tamanho=101",
            "/api/tarefas?status=INVALIDO", "/api/tarefas?pagina=abc"})
    void parametrosInvalidosRetornam400(String url) throws Exception {
        mvc.perform(get(url)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void sqlNaoPermiteStatusInvalidoMesmoForaDaApi() {
        assertThatThrownBy(() -> jdbc.update("INSERT INTO tarefas (nome, status) VALUES (?, ?)",
                "Teste", "INVALIDO")).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(repository.count()).isZero();
    }

    @Test
    void sqlNaoPermiteNomeVazioMesmoForaDaApi() {
        assertThatThrownBy(() -> jdbc.update("INSERT INTO tarefas (nome) VALUES (?)", " "))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(repository.count()).isZero();
    }

    @Test
    void sqlNaoPermiteAtualizacaoAnteriorACriacao() {
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO tarefas (nome, data_criacao, data_atualizacao)
                VALUES ('Teste', '2026-01-02T00:00:00Z', '2026-01-01T00:00:00Z')
                """)).isInstanceOf(DataIntegrityViolationException.class);
    }

    private JsonNode criar(String nome, String status) throws Exception {
        Map<String, String> campos = status == null ? Map.of("nome", nome)
                : Map.of("nome", nome, "status", status);
        return json(mvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(campos)))
                .andExpect(status().isCreated()).andReturn());
    }

    private JsonNode json(MvcResult result) throws Exception {
        return mapper.readTree(result.getResponse().getContentAsByteArray());
    }
}
