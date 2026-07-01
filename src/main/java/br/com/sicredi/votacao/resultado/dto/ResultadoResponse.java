package br.com.sicredi.votacao.resultado.dto;

import br.com.sicredi.votacao.resultado.ResultadoVotacao;
import br.com.sicredi.votacao.resultado.StatusVotacao;

import java.util.UUID;

public record ResultadoResponse(
        UUID pautaId,
        StatusVotacao status,
        long totalVotos,
        long votosSim,
        long votosNao,
        ResultadoVotacao resultado
) {
}
