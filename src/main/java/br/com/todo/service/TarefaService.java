package br.com.todo.service;

import br.com.todo.dto.AtualizarTarefaRequest;
import br.com.todo.dto.CriarTarefaRequest;
import br.com.todo.dto.PaginaTarefasResponse;
import br.com.todo.dto.TarefaResponse;
import br.com.todo.exception.TarefaNaoEncontradaException;
import br.com.todo.model.StatusTarefa;
import br.com.todo.model.Tarefa;
import br.com.todo.repository.TarefaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TarefaService {
    private final TarefaRepository repository;

    public TarefaService(TarefaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TarefaResponse criar(CriarTarefaRequest request) {
        Tarefa tarefa = new Tarefa(request.nome(), request.descricao(), request.status(),
                request.observacoes());
        return TarefaResponse.de(repository.saveAndFlush(tarefa));
    }

    public PaginaTarefasResponse listar(StatusTarefa status, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("id").descending());
        Page<Tarefa> tarefas = status == null
                ? repository.findAll(pageable)
                : repository.findByStatus(status, pageable);
        return PaginaTarefasResponse.de(tarefas.map(TarefaResponse::de));
    }

    public TarefaResponse buscar(Long id) {
        return TarefaResponse.de(encontrar(id));
    }

    @Transactional
    public TarefaResponse atualizar(Long id, AtualizarTarefaRequest request) {
        Tarefa tarefa = encontrar(id);
        tarefa.atualizar(request.nome(), request.descricao(), request.status(),
                request.observacoes());
        return TarefaResponse.de(repository.saveAndFlush(tarefa));
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(encontrar(id));
    }

    private Tarefa encontrar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }
}
