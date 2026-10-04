package br.com.todo.repository;

import br.com.todo.model.StatusTarefa;
import br.com.todo.model.Tarefa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    Page<Tarefa> findByStatus(StatusTarefa status, Pageable pageable);
}
