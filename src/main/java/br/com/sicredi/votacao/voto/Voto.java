package br.com.sicredi.votacao.voto;

import br.com.sicredi.votacao.pauta.Pauta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "votos")
public class Voto {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pauta_id", nullable = false)
    private Pauta pauta;

    @Column(name = "associado_id", nullable = false, length = 80)
    private String associadoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private OpcaoVoto opcao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Voto() {
    }

    public Voto(UUID id, Pauta pauta, String associadoId, OpcaoVoto opcao, LocalDateTime createdAt) {
        this.id = id;
        this.pauta = pauta;
        this.associadoId = associadoId;
        this.opcao = opcao;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Pauta getPauta() {
        return pauta;
    }

    public String getAssociadoId() {
        return associadoId;
    }

    public OpcaoVoto getOpcao() {
        return opcao;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
