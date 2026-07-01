package br.com.sicredi.votacao.pauta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarPautaRequest(
        @NotBlank
        @Size(max = 120)
        String titulo,

        @Size(max = 500)
        String descricao
) {
}
