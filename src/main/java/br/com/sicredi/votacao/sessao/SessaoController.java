package br.com.sicredi.votacao.sessao;

import br.com.sicredi.votacao.common.error.ApiErrorResponse;
import br.com.sicredi.votacao.sessao.dto.AbrirSessaoRequest;
import br.com.sicredi.votacao.sessao.dto.SessaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Sessoes de votacao", description = "Abertura de sessoes de votacao")
public class SessaoController {

    private final SessaoService sessaoService;

    public SessaoController(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @PostMapping
    @Operation(summary = "Abrir sessao de votacao", description = "Abre uma sessao de votacao para uma pauta existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sessao aberta",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SessaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisicao invalida",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pauta inexistente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Sessao ja aberta para a pauta",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<SessaoResponse> abrir(
            @Parameter(description = "Identificador da pauta", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
            @PathVariable UUID pautaId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Duracao opcional da sessao. Se ausente, usa 1 minuto.")
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
