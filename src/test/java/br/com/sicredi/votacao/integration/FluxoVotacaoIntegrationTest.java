package br.com.sicredi.votacao.integration;

import br.com.sicredi.votacao.pauta.Pauta;
import br.com.sicredi.votacao.pauta.PautaRepository;
import br.com.sicredi.votacao.sessao.SessaoRepository;
import br.com.sicredi.votacao.sessao.SessaoVotacao;
import br.com.sicredi.votacao.voto.OpcaoVoto;
import br.com.sicredi.votacao.voto.Voto;
import br.com.sicredi.votacao.voto.VotoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoVotacaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PautaRepository pautaRepository;

    @Autowired
    private SessaoRepository sessaoRepository;

    @Autowired
    private VotoRepository votoRepository;

    @BeforeEach
    void setUp() {
        votoRepository.deleteAll();
        sessaoRepository.deleteAll();
        pautaRepository.deleteAll();
    }

    @Test
    void deveExecutarFluxoCompletoComSessaoAberta() throws Exception {
        JsonNode pauta = criarPautaViaApi("Pauta fluxo completo", "Validacao do fluxo principal");
        String pautaId = pauta.get("id").asText();

        abrirSessaoViaApi(pautaId, 10);

        registrarVotoViaApi(pautaId, "22222222222", "SIM");
        registrarVotoViaApi(pautaId, "33333333333", "NAO");

        mockMvc.perform(get("/api/v1/pautas/{pautaId}/resultado", pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pautaId").value(pautaId))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.totalVotos").value(2))
                .andExpect(jsonPath("$.votosSim").value(1))
                .andExpect(jsonPath("$.votosNao").value(1))
                .andExpect(jsonPath("$.resultado").value("NAO_FINALIZADA"));
    }

    @Test
    void deveRetornarResultadoFinalAprovadoParaSessaoEncerrada() throws Exception {
        Pauta pauta = criarPautaPersistida();
        criarSessaoEncerrada(pauta);
        criarVotoPersistido(pauta, "associado-1", OpcaoVoto.SIM);
        criarVotoPersistido(pauta, "associado-2", OpcaoVoto.SIM);
        criarVotoPersistido(pauta, "associado-3", OpcaoVoto.NAO);

        mockMvc.perform(get("/api/v1/pautas/{pautaId}/resultado", pauta.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pautaId").value(pauta.getId().toString()))
                .andExpect(jsonPath("$.status").value("ENCERRADA"))
                .andExpect(jsonPath("$.totalVotos").value(3))
                .andExpect(jsonPath("$.votosSim").value(2))
                .andExpect(jsonPath("$.votosNao").value(1))
                .andExpect(jsonPath("$.resultado").value("APROVADA"));
    }

    @Test
    void deveImpedirVotoDuplicado() throws Exception {
        JsonNode pauta = criarPautaViaApi("Pauta voto duplicado", "Validacao de duplicidade");
        String pautaId = pauta.get("id").asText();
        abrirSessaoViaApi(pautaId, 10);

        registrarVotoViaApi(pautaId, "22222222222", "SIM");

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "associadoId", "22222222222",
                                "opcao", "NAO"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Associado ja votou nesta pauta"));
    }

    @Test
    void deveRetornarNotFoundQuandoCpfForInvalidoNoRegistroDeVoto() throws Exception {
        JsonNode pauta = criarPautaViaApi("Pauta CPF invalido", "Validacao de elegibilidade");
        String pautaId = pauta.get("id").asText();
        abrirSessaoViaApi(pautaId, 10);

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "associadoId", "00000000000",
                                "opcao", "SIM"
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("CPF invalido"));
    }

    @Test
    void deveRetornarForbiddenQuandoAssociadoNaoPodeVotar() throws Exception {
        JsonNode pauta = criarPautaViaApi("Pauta associado inapto", "Validacao de elegibilidade");
        String pautaId = pauta.get("id").asText();
        abrirSessaoViaApi(pautaId, 10);

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "associadoId", "11111111111",
                                "opcao", "SIM"
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Associado nao pode votar"));
    }

    @Test
    void deveImpedirVotoEmSessaoEncerrada() throws Exception {
        Pauta pauta = criarPautaPersistida();
        criarSessaoEncerrada(pauta);

        mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pauta.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "associadoId", "associado-tarde",
                                "opcao", "SIM"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Sessao de votacao nao esta aberta"));
    }

    @Test
    void deveRetornarResultadoNaoIniciadoQuandoNaoHaSessao() throws Exception {
        JsonNode pauta = criarPautaViaApi("Pauta sem sessao", "Resultado nao iniciado");
        String pautaId = pauta.get("id").asText();

        mockMvc.perform(get("/api/v1/pautas/{pautaId}/resultado", pautaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pautaId").value(pautaId))
                .andExpect(jsonPath("$.status").value("NAO_INICIADA"))
                .andExpect(jsonPath("$.totalVotos").value(0))
                .andExpect(jsonPath("$.votosSim").value(0))
                .andExpect(jsonPath("$.votosNao").value(0))
                .andExpect(jsonPath("$.resultado").value("NAO_FINALIZADA"));
    }

    @Test
    void deveRetornarNotFoundQuandoPautaNaoExistir() throws Exception {
        UUID pautaId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/pautas/{pautaId}/resultado", pautaId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Pauta nao encontrada"));
    }

    @Test
    void deveRetornarBadRequestQuandoUuidForInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/pautas/abc/resultado"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Parametro invalido"));
    }

    private JsonNode criarPautaViaApi(String titulo, String descricao) throws Exception {
        String content = mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "titulo", titulo,
                                "descricao", descricao
                        ))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.titulo").value(titulo))
                .andExpect(jsonPath("$.descricao").value(descricao))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(content);
    }

    private JsonNode abrirSessaoViaApi(String pautaId, int duracaoEmMinutos) throws Exception {
        String content = mockMvc.perform(post("/api/v1/pautas/{pautaId}/sessoes", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("duracaoEmMinutos", duracaoEmMinutos))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.pautaId").value(pautaId))
                .andExpect(jsonPath("$.openedAt").exists())
                .andExpect(jsonPath("$.closesAt").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(content);
    }

    private JsonNode registrarVotoViaApi(String pautaId, String associadoId, String opcao) throws Exception {
        String content = mockMvc.perform(post("/api/v1/pautas/{pautaId}/votos", pautaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "associadoId", associadoId,
                                "opcao", opcao
                        ))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.pautaId").value(pautaId))
                .andExpect(jsonPath("$.associadoId").value(associadoId))
                .andExpect(jsonPath("$.opcao").value(opcao))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(content);
    }

    private Pauta criarPautaPersistida() {
        return pautaRepository.save(new Pauta(
                UUID.randomUUID(),
                "Pauta encerrada",
                "Cenario de integracao",
                LocalDateTime.now().minusMinutes(20)
        ));
    }

    private SessaoVotacao criarSessaoEncerrada(Pauta pauta) {
        LocalDateTime openedAt = LocalDateTime.now().minusMinutes(10);
        return sessaoRepository.save(new SessaoVotacao(
                UUID.randomUUID(),
                pauta,
                openedAt,
                openedAt.plusMinutes(5),
                openedAt
        ));
    }

    private Voto criarVotoPersistido(Pauta pauta, String associadoId, OpcaoVoto opcao) {
        return votoRepository.save(new Voto(
                UUID.randomUUID(),
                pauta,
                associadoId,
                opcao,
                LocalDateTime.now().minusMinutes(6)
        ));
    }
}
