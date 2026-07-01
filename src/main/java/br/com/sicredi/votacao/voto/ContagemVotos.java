package br.com.sicredi.votacao.voto;

public record ContagemVotos(
        Long votosSim,
        Long votosNao,
        Long totalVotos
) {
}
