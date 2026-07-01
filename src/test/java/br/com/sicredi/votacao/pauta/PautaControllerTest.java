package br.com.sicredi.votacao.pauta;

import br.com.sicredi.votacao.pauta.dto.CriarPautaRequest;
import br.com.sicredi.votacao.pauta.dto.PautaResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PautaController.class)
class PautaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PautaService pautaService;

    @Test
    void deveCriarPautaERetornarCreated() throws Exception {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 30, 21, 10);
        Mockito.when(pautaService.criar(any(CriarPautaRequest.class)))
                .thenReturn(new PautaResponse(id, "Assembleia ordinaria", "Discussao anual", createdAt));

        CriarPautaRequest request = new CriarPautaRequest("Assembleia ordinaria", "Discussao anual");

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/pautas/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.titulo").value("Assembleia ordinaria"))
                .andExpect(jsonPath("$.descricao").value("Discussao anual"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-30T21:10:00"));
    }

    @Test
    void deveRetornarBadRequestQuandoTituloForBlank() throws Exception {
        CriarPautaRequest request = new CriarPautaRequest(" ", "Discussao anual");

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("titulo: must not be blank"))
                .andExpect(jsonPath("$.path").value("/api/v1/pautas"));
    }

    @Test
    void deveRetornarBadRequestQuandoTituloExcederLimite() throws Exception {
        CriarPautaRequest request = new CriarPautaRequest("a".repeat(121), "Discussao anual");

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void deveRetornarBadRequestQuandoDescricaoExcederLimite() throws Exception {
        CriarPautaRequest request = new CriarPautaRequest("Assembleia ordinaria", "a".repeat(501));

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
