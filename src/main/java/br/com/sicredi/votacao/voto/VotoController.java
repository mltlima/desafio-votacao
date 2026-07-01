package br.com.sicredi.votacao.voto;

import br.com.sicredi.votacao.voto.dto.RegistrarVotoRequest;
import br.com.sicredi.votacao.voto.dto.VotoResponse;
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
@RequestMapping("/api/v1/pautas/{pautaId}/votos")
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    @PostMapping
    public ResponseEntity<VotoResponse> registrar(
            @PathVariable UUID pautaId,
            @Valid @RequestBody RegistrarVotoRequest request
    ) {
        VotoResponse response = votoService.registrar(pautaId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{votoId}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
