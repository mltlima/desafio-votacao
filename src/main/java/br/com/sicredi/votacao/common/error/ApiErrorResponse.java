package br.com.sicredi.votacao.common.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resposta padrao de erro da API")
public record ApiErrorResponse(
        @Schema(description = "Data e hora em que o erro ocorreu", example = "2026-07-01T10:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Codigo HTTP do erro", example = "409")
        int status,

        @Schema(description = "Descricao padrao do status HTTP", example = "Conflict")
        String error,

        @Schema(description = "Mensagem detalhada do erro", example = "Associado ja votou nesta pauta")
        String message,

        @Schema(description = "Caminho da requisicao", example = "/api/v1/pautas/550e8400-e29b-41d4-a716-446655440000/votos")
        String path
) {
}
