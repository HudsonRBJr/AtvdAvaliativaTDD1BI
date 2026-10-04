package br.com.todo.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(TarefaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> naoEncontrada(TarefaNaoEncontradaException ex,
                                                     HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validarCorpo(MethodArgumentNotValidException ex,
                                                    HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                campos.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos.", request, campos);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResponse> validarParametros(ConstraintViolationException ex,
                                                         HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String campo = violation.getPropertyPath().toString();
            campos.put(campo.substring(campo.lastIndexOf('.') + 1), violation.getMessage());
        });
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetros inválidos.", request, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST,
                "Corpo JSON inválido. Verifique os campos e os valores de status.", request, Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tipoInvalido(MethodArgumentTypeMismatchException ex,
                                                   HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + ex.getName() + ".",
                request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT,
                "Os dados não atendem às regras de integridade do banco.", request, Map.of());
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem,
                                                 HttpServletRequest request, Map<String, String> campos) {
        ErroResponse erro = new ErroResponse(OffsetDateTime.now(ZoneOffset.UTC), status.value(),
                mensagem, request.getRequestURI(), campos);
        return ResponseEntity.status(status).body(erro);
    }
}
