package br.com.todo.dto;

import br.com.todo.model.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarTarefaRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,
        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres.")
        String descricao,
        @NotNull(message = "O status é obrigatório na atualização.")
        StatusTarefa status,
        @Size(max = 2000, message = "As observações devem ter no máximo 2000 caracteres.")
        String observacoes) {
}
