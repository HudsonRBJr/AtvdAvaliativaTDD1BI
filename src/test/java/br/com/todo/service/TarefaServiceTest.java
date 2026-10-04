package br.com.todo.service;

import br.com.todo.dto.AtualizarTarefaRequest;
import br.com.todo.dto.CriarTarefaRequest;
import br.com.todo.dto.PaginaTarefasResponse;
import br.com.todo.dto.TarefaResponse;
import br.com.todo.exception.TarefaNaoEncontradaException;
import br.com.todo.model.StatusTarefa;
import br.com.todo.model.Tarefa;
import br.com.todo.repository.TarefaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {
    @Mock
    private TarefaRepository repository;

    @InjectMocks
    private TarefaService service;

    @Test
    void criarSemStatusUsaPendenteEConservaOsCampos() {
        when(repository.saveAndFlush(any(Tarefa.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TarefaResponse resposta = service.criar(new CriarTarefaRequest(
                "  Estudar Java  ", "Revisar serviços", null, "Fazer exercícios"));

        assertThat(resposta.nome()).isEqualTo("Estudar Java");
        assertThat(resposta.status()).isEqualTo(StatusTarefa.PENDENTE);
        assertThat(resposta.descricao()).isEqualTo("Revisar serviços");
        assertThat(resposta.observacoes()).isEqualTo("Fazer exercícios");
        verify(repository).saveAndFlush(any(Tarefa.class));
    }

    @Test
    void criarComStatusInformadoPreservaOStatus() {
        when(repository.saveAndFlush(any(Tarefa.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TarefaResponse resposta = service.criar(new CriarTarefaRequest(
                "Entregar atividade", null, StatusTarefa.CONCLUIDA, null));
        assertThat(resposta.status()).isEqualTo(StatusTarefa.CONCLUIDA);
    }

    @Test
    void buscarRetornaATarefaExistente() {
        when(repository.findById(10L)).thenReturn(Optional.of(tarefa()));
        assertThat(service.buscar(10L).nome()).isEqualTo("Estudar Java");
    }

    @Test
    void buscarInexistenteLancaExcecao() {
        when(repository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscar(10L))
                .isInstanceOf(TarefaNaoEncontradaException.class)
                .hasMessage("Tarefa 10 não encontrada.");
    }

    @Test
    void atualizarSubstituiTodosOsCamposEditaveis() {
        Tarefa tarefa = tarefa();
        when(repository.findById(10L)).thenReturn(Optional.of(tarefa));
        when(repository.saveAndFlush(tarefa)).thenReturn(tarefa);

        TarefaResponse resposta = service.atualizar(10L, new AtualizarTarefaRequest(
                "  Revisar banco  ", "PostgreSQL", StatusTarefa.EM_ANDAMENTO, "Em revisão"));

        assertThat(resposta.nome()).isEqualTo("Revisar banco");
        assertThat(resposta.descricao()).isEqualTo("PostgreSQL");
        assertThat(resposta.status()).isEqualTo(StatusTarefa.EM_ANDAMENTO);
        assertThat(resposta.observacoes()).isEqualTo("Em revisão");
        verify(repository).saveAndFlush(tarefa);
    }

    @Test
    void atualizarPodeLimparOsCamposOpcionais() {
        Tarefa tarefa = tarefa();
        when(repository.findById(10L)).thenReturn(Optional.of(tarefa));
        when(repository.saveAndFlush(tarefa)).thenReturn(tarefa);
        TarefaResponse resposta = service.atualizar(10L,
                new AtualizarTarefaRequest("Estudar Java", null, StatusTarefa.PENDENTE, null));
        assertThat(resposta.descricao()).isNull();
        assertThat(resposta.observacoes()).isNull();
    }

    @Test
    void atualizarInexistenteNaoInsereOutraTarefa() {
        when(repository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.atualizar(10L,
                new AtualizarTarefaRequest("Nova", null, StatusTarefa.PENDENTE, null)))
                .isInstanceOf(TarefaNaoEncontradaException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void excluirRemoveATarefaExistente() {
        Tarefa tarefa = tarefa();
        when(repository.findById(10L)).thenReturn(Optional.of(tarefa));
        service.excluir(10L);
        verify(repository).delete(tarefa);
    }

    @Test
    void excluirInexistenteLancaExcecao() {
        when(repository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.excluir(10L))
                .isInstanceOf(TarefaNaoEncontradaException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    void listarSemFiltroUsaPaginacaoEOrdemDecrescente() {
        when(repository.findAll(any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(tarefa()), invocation.getArgument(0), 5));
        PaginaTarefasResponse resposta = service.listar(null, 1, 2);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(2);
        assertThat(captor.getValue().getSort().getOrderFor("id").isDescending()).isTrue();
        assertThat(resposta.itens()).hasSize(1);
        assertThat(resposta.totalElementos()).isEqualTo(5);
        assertThat(resposta.totalPaginas()).isEqualTo(3);
    }

    @Test
    void listarComFiltroConsultaApenasOStatusSolicitado() {
        when(repository.findByStatus(eq(StatusTarefa.PENDENTE), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(tarefa()), invocation.getArgument(1), 1));
        assertThat(service.listar(StatusTarefa.PENDENTE, 0, 20).itens()).hasSize(1);
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    void listarPodeRetornarPaginaVazia() {
        when(repository.findAll(any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(0), 0));
        PaginaTarefasResponse resposta = service.listar(null, 0, 20);
        assertThat(resposta.itens()).isEmpty();
        assertThat(resposta.totalElementos()).isZero();
    }

    private Tarefa tarefa() {
        return new Tarefa("Estudar Java", "Revisar serviços", StatusTarefa.PENDENTE, "Praticar");
    }
}
