package br.com.sicredi.votacao.voto.dto;

import br.com.sicredi.votacao.voto.OpcaoVoto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarVotoRequest(
        @NotBlank
        @Size(max = 80)
        String associadoId,

        @NotNull
        OpcaoVoto opcao
) {
}
