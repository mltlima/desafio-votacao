package br.com.sicredi.votacao.sessao.dto;

import br.com.sicredi.votacao.sessao.SessaoVotacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Sessao de votacao aberta")
public record SessaoResponse(
        @Schema(description = "Identificador da sessao", example = "9a3f9ff1-47b6-44fa-9f6f-0f0f581dfc01")
        UUID id,

        @Schema(description = "Identificador da pauta", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID pautaId,

        @Schema(description = "Data e hora de abertura da sessao", example = "2026-07-01T10:30:00")
        LocalDateTime openedAt,

        @Schema(description = "Data e hora de encerramento da sessao", example = "2026-07-01T10:40:00")
        LocalDateTime closesAt,

        @Schema(description = "Data e hora de criacao do registro", example = "2026-07-01T10:30:00")
        LocalDateTime createdAt
) {

    public static SessaoResponse from(SessaoVotacao sessao) {
        return new SessaoResponse(
                sessao.getId(),
                sessao.getPauta().getId(),
                sessao.getOpenedAt(),
                sessao.getClosesAt(),
                sessao.getCreatedAt()
        );
    }
}
