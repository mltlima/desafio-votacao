package br.com.sicredi.votacao.sessao.dto;

import jakarta.validation.constraints.Positive;

public record AbrirSessaoRequest(
        @Positive
        Integer duracaoEmMinutos
) {
}
