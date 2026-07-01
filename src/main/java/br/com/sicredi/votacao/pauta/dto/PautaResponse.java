package br.com.sicredi.votacao.pauta.dto;

import br.com.sicredi.votacao.pauta.Pauta;

import java.time.LocalDateTime;
import java.util.UUID;

public record PautaResponse(
        UUID id,
        String titulo,
        String descricao,
        LocalDateTime createdAt
) {

    public static PautaResponse from(Pauta pauta) {
        return new PautaResponse(
                pauta.getId(),
                pauta.getTitulo(),
                pauta.getDescricao(),
                pauta.getCreatedAt()
        );
    }
}
