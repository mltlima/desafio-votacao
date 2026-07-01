package br.com.sicredi.votacao.resultado;

import br.com.sicredi.votacao.resultado.dto.ResultadoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/resultado")
public class ResultadoController {

    private final ResultadoService resultadoService;

    public ResultadoController(ResultadoService resultadoService) {
        this.resultadoService = resultadoService;
    }

    @GetMapping
    public ResponseEntity<ResultadoResponse> consultar(@PathVariable UUID pautaId) {
        return ResponseEntity.ok(resultadoService.consultar(pautaId));
    }
}
