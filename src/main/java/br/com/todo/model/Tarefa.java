package br.com.todo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "tarefas")
public class Tarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTarefa status;

    @Column(length = 2000)
    private String observacoes;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private OffsetDateTime dataAtualizacao;

    protected Tarefa() {
        // Construtor exigido pelo JPA.
    }

    public Tarefa(String nome, String descricao, StatusTarefa status, String observacoes) {
        this.nome = nome.strip();
        this.descricao = descricao;
        this.status = status == null ? StatusTarefa.PENDENTE : status;
        this.observacoes = observacoes;
    }

    public void atualizar(String nome, String descricao, StatusTarefa status, String observacoes) {
        this.nome = nome.strip();
        this.descricao = descricao;
        this.status = status;
        this.observacoes = observacoes;
    }

    @PrePersist
    private void aoCriar() {
        OffsetDateTime agora = agoraUtc();
        this.dataCriacao = agora;
        this.dataAtualizacao = agora;
    }

    @PreUpdate
    private void aoAtualizar() {
        this.dataAtualizacao = agoraUtc();
    }

    private static OffsetDateTime agoraUtc() {
        return OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public StatusTarefa getStatus() { return status; }
    public String getObservacoes() { return observacoes; }
    public OffsetDateTime getDataCriacao() { return dataCriacao; }
    public OffsetDateTime getDataAtualizacao() { return dataAtualizacao; }
}
