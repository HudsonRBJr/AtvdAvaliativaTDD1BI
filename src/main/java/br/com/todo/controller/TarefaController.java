package br.com.todo.controller;

import br.com.todo.dto.AtualizarTarefaRequest;
import br.com.todo.dto.CriarTarefaRequest;
import br.com.todo.dto.PaginaTarefasResponse;
import br.com.todo.dto.TarefaResponse;
import br.com.todo.model.StatusTarefa;
import br.com.todo.service.TarefaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/tarefas")
@Validated
public class TarefaController {
    private final TarefaService service;

    public TarefaController(TarefaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TarefaResponse> criar(@Valid @RequestBody CriarTarefaRequest request) {
        TarefaResponse tarefa = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(tarefa.id()).toUri();
        return ResponseEntity.created(location).body(tarefa);
    }

    @GetMapping
    public PaginaTarefasResponse listar(
            @RequestParam(required = false) StatusTarefa status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "A página deve ser zero ou maior.") int pagina,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "O tamanho mínimo é 1.")
            @Max(value = 100, message = "O tamanho máximo é 100.") int tamanho) {
        return service.listar(status, pagina, tamanho);
    }

    @GetMapping("/{id}")
    public TarefaResponse buscar(@PathVariable @Positive(message = "O ID deve ser positivo.") Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public TarefaResponse atualizar(@PathVariable @Positive(message = "O ID deve ser positivo.") Long id,
                                   @Valid @RequestBody AtualizarTarefaRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive(message = "O ID deve ser positivo.") Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
