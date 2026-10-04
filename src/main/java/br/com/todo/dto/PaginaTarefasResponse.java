package br.com.todo.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record PaginaTarefasResponse(List<TarefaResponse> itens, int pagina, int tamanho,
                                    long totalElementos, int totalPaginas) {
    public static PaginaTarefasResponse de(Page<TarefaResponse> pagina) {
        return new PaginaTarefasResponse(pagina.getContent(), pagina.getNumber(),
                pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages());
    }
}
