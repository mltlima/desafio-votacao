package br.com.sicredi.votacao.sessao;

import br.com.sicredi.votacao.sessao.dto.AbrirSessaoRequest;
import br.com.sicredi.votacao.sessao.dto.SessaoResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/sessoes")
public class SessaoController {

    private final SessaoService sessaoService;

    public SessaoController(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @PostMapping
    public ResponseEntity<SessaoResponse> abrir(
            @PathVariable UUID pautaId,
            @Valid @RequestBody(required = false) AbrirSessaoRequest request
    ) {
        SessaoResponse response = sessaoService.abrir(pautaId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{sessaoId}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
