package br.com.todo.dto;

import br.com.todo.model.StatusTarefa;
import br.com.todo.model.Tarefa;
import java.time.OffsetDateTime;

public record TarefaResponse(Long id, String nome, String descricao, StatusTarefa status,
                             String observacoes, OffsetDateTime dataCriacao,
                             OffsetDateTime dataAtualizacao) {
    public static TarefaResponse de(Tarefa tarefa) {
        return new TarefaResponse(tarefa.getId(), tarefa.getNome(), tarefa.getDescricao(),
                tarefa.getStatus(), tarefa.getObservacoes(), tarefa.getDataCriacao(),
                tarefa.getDataAtualizacao());
    }
}
