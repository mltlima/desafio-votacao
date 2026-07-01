package br.com.sicredi.votacao.sessao;

import br.com.sicredi.votacao.sessao.dto.AbrirSessaoRequest;
import br.com.sicredi.votacao.sessao.dto.SessaoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessaoController.class)
class SessaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SessaoService sessaoService;

    @Test
    void deveAbrirSessaoERetornarCreated() throws Exception {
        UUID pautaId = UUID.randomUUID();
        UUID sessaoId = UUID.randomUUID();
        LocalDateTime openedAt = LocalDateTime.of(2026, 6, 30, 21, 30);
        Mockito.when(sessaoService.abrir(eq(pautaId), any(AbrirSessaoRequest.class)))
                .thenReturn(new SessaoResponse(sessaoId, pautaId, openedAt, openedAt.plusMinutes(10), openedAt));

        AbrirSessaoRequest request = new AbrirSessaoRequest(10);

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessoes", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/pautas/" + pautaId + "/sessoes/" + sessaoId)))
                .andExpect(jsonPath("$.id").value(sessaoId.toString()))
                .andExpect(jsonPath("$.pautaId").value(pautaId.toString()))
                .andExpect(jsonPath("$.openedAt").value("2026-06-30T21:30:00"))
                .andExpect(jsonPath("$.closesAt").value("2026-06-30T21:40:00"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-30T21:30:00"));
    }

    @Test
    void deveAceitarBodyAusenteParaDuracaoPadrao() throws Exception {
        UUID pautaId = UUID.randomUUID();
        UUID sessaoId = UUID.randomUUID();
        LocalDateTime openedAt = LocalDateTime.of(2026, 6, 30, 21, 30);
        Mockito.when(sessaoService.abrir(eq(pautaId), eq(null)))
                .thenReturn(new SessaoResponse(sessaoId, pautaId, openedAt, openedAt.plusMinutes(1), openedAt));

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessoes", pautaId))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/pautas/" + pautaId + "/sessoes/" + sessaoId)))
                .andExpect(jsonPath("$.closesAt").value("2026-06-30T21:31:00"));
    }

    @Test
    void deveAceitarBodyVazioParaDuracaoPadrao() throws Exception {
        UUID pautaId = UUID.randomUUID();
        UUID sessaoId = UUID.randomUUID();
        LocalDateTime openedAt = LocalDateTime.of(2026, 6, 30, 21, 30);
        Mockito.when(sessaoService.abrir(eq(pautaId), any(AbrirSessaoRequest.class)))
                .thenReturn(new SessaoResponse(sessaoId, pautaId, openedAt, openedAt.plusMinutes(1), openedAt));

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessoes", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.closesAt").value("2026-06-30T21:31:00"));
    }

    @Test
    void deveRetornarBadRequestQuandoDuracaoNaoForPositiva() throws Exception {
        UUID pautaId = UUID.randomUUID();
        AbrirSessaoRequest request = new AbrirSessaoRequest(0);

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessoes", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRefletirNotFoundLancadoPeloService() throws Exception {
        UUID pautaId = UUID.randomUUID();
        Mockito.when(sessaoService.abrir(eq(pautaId), any(AbrirSessaoRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pauta nao encontrada"));

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessoes", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AbrirSessaoRequest(10))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRefletirConflictLancadoPeloService() throws Exception {
        UUID pautaId = UUID.randomUUID();
        Mockito.when(sessaoService.abrir(eq(pautaId), any(AbrirSessaoRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Sessao ja aberta para esta pauta"));

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessoes", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AbrirSessaoRequest(10))))
                .andExpect(status().isConflict());
    }
}
