package br.com.sicredi.votacao.sessao;

import br.com.sicredi.votacao.pauta.Pauta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessoes_votacao")
public class SessaoVotacao {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pauta_id", nullable = false)
    private Pauta pauta;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closes_at", nullable = false)
    private LocalDateTime closesAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected SessaoVotacao() {
    }

    public SessaoVotacao(UUID id, Pauta pauta, LocalDateTime openedAt, LocalDateTime closesAt, LocalDateTime createdAt) {
        this.id = id;
        this.pauta = pauta;
        this.openedAt = openedAt;
        this.closesAt = closesAt;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Pauta getPauta() {
        return pauta;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public LocalDateTime getClosesAt() {
        return closesAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
