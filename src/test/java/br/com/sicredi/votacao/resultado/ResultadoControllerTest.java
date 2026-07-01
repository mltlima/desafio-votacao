package br.com.sicredi.votacao.resultado;

import br.com.sicredi.votacao.resultado.dto.ResultadoResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResultadoController.class)
class ResultadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResultadoService resultadoService;

    @Test
    void deveRetornarResultadoDaVotacao() throws Exception {
        UUID pautaId = UUID.randomUUID();
        when(resultadoService.consultar(pautaId))
                .thenReturn(new ResultadoResponse(
                        pautaId,
                        StatusVotacao.ENCERRADA,
                        10,
                        6,
                        4,
                        ResultadoVotacao.APROVADA
                ));

        mockMvc.perform(get("/api/v1/pautas/{pautaId}/resultado", pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pautaId").value(pautaId.toString()))
                .andExpect(jsonPath("$.status").value("ENCERRADA"))
                .andExpect(jsonPath("$.totalVotos").value(10))
                .andExpect(jsonPath("$.votosSim").value(6))
                .andExpect(jsonPath("$.votosNao").value(4))
                .andExpect(jsonPath("$.resultado").value("APROVADA"));
    }

    @Test
    void deveRefletirNotFoundLancadoPeloService() throws Exception {
        UUID pautaId = UUID.randomUUID();
        Mockito.when(resultadoService.consultar(pautaId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pauta nao encontrada"));

        mockMvc.perform(get("/api/v1/pautas/{pautaId}/resultado", pautaId))
                .andExpect(status().isNotFound());
    }
}
