package br.com.todo.exception;

public class TarefaNaoEncontradaException extends RuntimeException {
    public TarefaNaoEncontradaException(Long id) {
        super("Tarefa " + id + " não encontrada.");
    }
}
