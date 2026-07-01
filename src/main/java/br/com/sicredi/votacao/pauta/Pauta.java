package br.com.sicredi.votacao.pauta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pautas")
public class Pauta {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(length = 500)
    private String descricao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Pauta() {
    }

    public Pauta(UUID id, String titulo, String descricao, LocalDateTime createdAt) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
