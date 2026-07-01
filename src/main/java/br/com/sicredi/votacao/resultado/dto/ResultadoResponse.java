package br.com.sicredi.votacao.resultado.dto;

import br.com.sicredi.votacao.resultado.ResultadoVotacao;
import br.com.sicredi.votacao.resultado.StatusVotacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resultado da votacao de uma pauta")
public record ResultadoResponse(
        @Schema(description = "Identificador da pauta", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID pautaId,

        @Schema(description = "Status atual da votacao", example = "ENCERRADA", allowableValues = {"NAO_INICIADA", "ABERTA", "ENCERRADA"})
        StatusVotacao status,

        @Schema(description = "Total de votos registrados", example = "10")
        long totalVotos,

        @Schema(description = "Quantidade de votos SIM", example = "6")
        long votosSim,

        @Schema(description = "Quantidade de votos NAO", example = "4")
        long votosNao,

        @Schema(description = "Resultado final quando a sessao estiver encerrada", example = "APROVADA", allowableValues = {"APROVADA", "REJEITADA", "EMPATADA", "NAO_FINALIZADA"})
        ResultadoVotacao resultado
) {
}
