package br.com.sicredi.votacao.sessao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "Dados para abertura de uma sessao de votacao")
public record AbrirSessaoRequest(
        @Schema(description = "Duracao da sessao em minutos. Quando ausente, usa 1 minuto.", example = "10", minimum = "1")
        @Positive
        Integer duracaoEmMinutos
) {
}
