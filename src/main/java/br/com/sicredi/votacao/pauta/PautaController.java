package br.com.sicredi.votacao.pauta;

import br.com.sicredi.votacao.common.error.ApiErrorResponse;
import br.com.sicredi.votacao.pauta.dto.CriarPautaRequest;
import br.com.sicredi.votacao.pauta.dto.PautaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pautas")
@Tag(name = "Pautas", description = "Cadastro de pautas para votacao")
public class PautaController {

    private final PautaService pautaService;

    public PautaController(PautaService pautaService) {
        this.pautaService = pautaService;
    }

    @PostMapping
    @Operation(summary = "Criar pauta", description = "Cria uma nova pauta para votacao.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pauta criada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PautaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisicao invalida",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PautaResponse> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados da pauta", required = true)
            @Valid @RequestBody CriarPautaRequest request
    ) {
        PautaResponse response = pautaService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
