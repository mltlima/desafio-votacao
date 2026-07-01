package br.com.sicredi.votacao.sessao.dto;

import br.com.sicredi.votacao.sessao.SessaoVotacao;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessaoResponse(
        UUID id,
        UUID pautaId,
        LocalDateTime openedAt,
        LocalDateTime closesAt,
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
