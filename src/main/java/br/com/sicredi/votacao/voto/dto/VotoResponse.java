package br.com.sicredi.votacao.voto.dto;

import br.com.sicredi.votacao.voto.OpcaoVoto;
import br.com.sicredi.votacao.voto.Voto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Voto registrado")
public record VotoResponse(
        @Schema(description = "Identificador do voto", example = "23f182c5-ec3f-42db-b72f-92c28069fca2")
        UUID id,

        @Schema(description = "Identificador da pauta", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID pautaId,

        @Schema(description = "Identificador do associado", example = "associado-123")
        String associadoId,

        @Schema(description = "Opcao escolhida", example = "SIM", allowableValues = {"SIM", "NAO"})
        OpcaoVoto opcao,

        @Schema(description = "Data e hora de registro do voto", example = "2026-07-01T10:35:00")
        LocalDateTime createdAt
) {

    public static VotoResponse from(Voto voto) {
        return new VotoResponse(
                voto.getId(),
                voto.getPauta().getId(),
                voto.getAssociadoId(),
                voto.getOpcao(),
                voto.getCreatedAt()
        );
    }
}
