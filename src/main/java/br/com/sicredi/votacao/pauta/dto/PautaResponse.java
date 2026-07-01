package br.com.sicredi.votacao.pauta.dto;

import br.com.sicredi.votacao.pauta.Pauta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Pauta criada")
public record PautaResponse(
        @Schema(description = "Identificador da pauta", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Titulo da pauta", example = "Assembleia ordinaria")
        String titulo,

        @Schema(description = "Descricao da pauta", example = "Discussao sobre orcamento anual")
        String descricao,

        @Schema(description = "Data e hora de criacao", example = "2026-07-01T10:30:00")
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
