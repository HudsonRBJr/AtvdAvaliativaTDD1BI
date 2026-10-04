package br.com.todo.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErroResponse(OffsetDateTime instante, int status, String mensagem,
                           String caminho, Map<String, String> campos) {
}
