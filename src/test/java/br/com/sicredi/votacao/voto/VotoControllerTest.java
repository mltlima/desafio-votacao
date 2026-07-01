package br.com.sicredi.votacao.voto;

import br.com.sicredi.votacao.common.exception.AssociadoNaoPodeVotarException;
import br.com.sicredi.votacao.common.exception.CpfInvalidoException;
import br.com.sicredi.votacao.common.exception.PautaNaoEncontradaException;
import br.com.sicredi.votacao.common.exception.VotoDuplicadoException;
import br.com.sicredi.votacao.voto.dto.RegistrarVotoRequest;
import br.com.sicredi.votacao.voto.dto.VotoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VotoController.class)
class VotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VotoService votoService;

    @Test
    void deveRegistrarVotoERetornarCreated() throws Exception {
        UUID pautaId = UUID.randomUUID();
        UUID votoId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 30, 21, 45);
        Mockito.when(votoService.registrar(eq(pautaId), any(RegistrarVotoRequest.class)))
                .thenReturn(new VotoResponse(votoId, pautaId, "associado-123", OpcaoVoto.SIM, createdAt));

        RegistrarVotoRequest request = new RegistrarVotoRequest("associado-123", OpcaoVoto.SIM);

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/pautas/" + pautaId + "/votos/" + votoId)))
                .andExpect(jsonPath("$.id").value(votoId.toString()))
                .andExpect(jsonPath("$.pautaId").value(pautaId.toString()))
                .andExpect(jsonPath("$.associadoId").value("associado-123"))
                .andExpect(jsonPath("$.opcao").value("SIM"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-30T21:45:00"));
    }

    @Test
    void deveRetornarBadRequestQuandoAssociadoIdForBlank() throws Exception {
        RegistrarVotoRequest request = new RegistrarVotoRequest(" ", OpcaoVoto.SIM);

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveRetornarBadRequestQuandoAssociadoIdExcederLimite() throws Exception {
        RegistrarVotoRequest request = new RegistrarVotoRequest("a".repeat(81), OpcaoVoto.SIM);

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveRetornarBadRequestQuandoOpcaoForAusente() throws Exception {
        Map<String, String> request = Map.of("associadoId", "associado-123");

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveRetornarBadRequestQuandoOpcaoForInvalida() throws Exception {
        Map<String, String> request = Map.of(
                "associadoId", "associado-123",
                "opcao", "TALVEZ"
        );

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Requisicao invalida"));
    }

    @Test
    void deveRefletirNotFoundLancadoPeloService() throws Exception {
        UUID pautaId = UUID.randomUUID();
        Mockito.when(votoService.registrar(eq(pautaId), any(RegistrarVotoRequest.class)))
                .thenThrow(new PautaNaoEncontradaException());

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistrarVotoRequest("associado-123", OpcaoVoto.SIM))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Pauta nao encontrada"))
                .andExpect(jsonPath("$.path").value("/api/v1/pautas/" + pautaId + "/votos"));
    }

    @Test
    void deveRefletirNotFoundQuandoCpfForInvalido() throws Exception {
        UUID pautaId = UUID.randomUUID();
        Mockito.when(votoService.registrar(eq(pautaId), any(RegistrarVotoRequest.class)))
                .thenThrow(new CpfInvalidoException());

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistrarVotoRequest("00000000000", OpcaoVoto.SIM))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("CPF invalido"))
                .andExpect(jsonPath("$.path").value("/api/v1/pautas/" + pautaId + "/votos"));
    }

    @Test
    void deveRefletirForbiddenQuandoAssociadoNaoPodeVotar() throws Exception {
        UUID pautaId = UUID.randomUUID();
        Mockito.when(votoService.registrar(eq(pautaId), any(RegistrarVotoRequest.class)))
                .thenThrow(new AssociadoNaoPodeVotarException());

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistrarVotoRequest("11111111111", OpcaoVoto.SIM))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Associado nao pode votar"))
                .andExpect(jsonPath("$.path").value("/api/v1/pautas/" + pautaId + "/votos"));
    }

    @Test
    void deveRefletirConflictLancadoPeloService() throws Exception {
        UUID pautaId = UUID.randomUUID();
        Mockito.when(votoService.registrar(eq(pautaId), any(RegistrarVotoRequest.class)))
                .thenThrow(new VotoDuplicadoException());

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistrarVotoRequest("associado-123", OpcaoVoto.SIM))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Associado ja votou nesta pauta"))
                .andExpect(jsonPath("$.path").value("/api/v1/pautas/" + pautaId + "/votos"));
    }
}
