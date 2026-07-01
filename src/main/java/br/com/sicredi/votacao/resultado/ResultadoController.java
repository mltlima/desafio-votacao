package br.com.sicredi.votacao.resultado;

import br.com.sicredi.votacao.common.error.ApiErrorResponse;
import br.com.sicredi.votacao.resultado.dto.ResultadoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/resultado")
@Tag(name = "Resultados", description = "Consulta do resultado da votacao")
public class ResultadoController {

    private final ResultadoService resultadoService;

    public ResultadoController(ResultadoService resultadoService) {
        this.resultadoService = resultadoService;
    }

    @GetMapping
    @Operation(summary = "Consultar resultado", description = "Consulta status, contagem de votos e resultado final de uma pauta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado retornado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResultadoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parametro invalido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pauta inexistente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno inesperado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ResultadoResponse> consultar(
            @Parameter(description = "Identificador da pauta", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
            @PathVariable UUID pautaId
    ) {
        return ResponseEntity.ok(resultadoService.consultar(pautaId));
    }
}
