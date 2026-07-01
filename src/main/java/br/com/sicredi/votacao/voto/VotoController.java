package br.com.sicredi.votacao.voto;

import br.com.sicredi.votacao.common.error.ApiErrorResponse;
import br.com.sicredi.votacao.voto.dto.RegistrarVotoRequest;
import br.com.sicredi.votacao.voto.dto.VotoResponse;
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
@RequestMapping("/api/v1/pautas/{pautaId}/votos")
@Tag(name = "Votos", description = "Registro de votos dos associados")
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    @PostMapping
    @Operation(summary = "Registrar voto", description = "Registra o voto de um associado em uma pauta com sessao aberta.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Voto registrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VotoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisicao invalida",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pauta inexistente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Sessao indisponivel ou voto duplicado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<VotoResponse> registrar(
            @Parameter(description = "Identificador da pauta", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
            @PathVariable UUID pautaId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do voto", required = true)
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
