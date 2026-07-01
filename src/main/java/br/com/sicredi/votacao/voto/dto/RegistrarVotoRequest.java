package br.com.sicredi.votacao.voto.dto;

import br.com.sicredi.votacao.voto.OpcaoVoto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para registro de voto")
public record RegistrarVotoRequest(
        @Schema(description = "Identificador do associado", example = "associado-123", maxLength = 80, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 80)
        String associadoId,

        @Schema(description = "Opcao do voto", example = "SIM", allowableValues = {"SIM", "NAO"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        OpcaoVoto opcao
) {
}
