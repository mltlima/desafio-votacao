package br.com.sicredi.votacao.voto.dto;

import br.com.sicredi.votacao.voto.OpcaoVoto;
import br.com.sicredi.votacao.voto.Voto;

import java.time.LocalDateTime;
import java.util.UUID;

public record VotoResponse(
        UUID id,
        UUID pautaId,
        String associadoId,
        OpcaoVoto opcao,
        LocalDateTime createdAt
) {

    public static VotoResponse from(Voto voto) {
        return new VotoResponse(
                voto.getId(),
                voto.getPauta().getId(),
                voto.getAssociadoId(),
                voto.getOpcao(),
                voto.getCreatedAt()
        );
    }
}
