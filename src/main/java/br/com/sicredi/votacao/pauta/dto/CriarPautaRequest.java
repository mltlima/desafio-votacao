package br.com.sicredi.votacao.pauta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criacao de uma pauta")
public record CriarPautaRequest(
        @Schema(description = "Titulo da pauta", example = "Assembleia ordinaria", maxLength = 120, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 120)
        String titulo,

        @Schema(description = "Descricao opcional da pauta", example = "Discussao sobre orcamento anual", maxLength = 500)
        @Size(max = 500)
        String descricao
) {
}
