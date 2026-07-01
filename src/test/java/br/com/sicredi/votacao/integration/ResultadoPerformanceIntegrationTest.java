package br.com.sicredi.votacao.integration;

import br.com.sicredi.votacao.pauta.Pauta;
import br.com.sicredi.votacao.pauta.PautaRepository;
import br.com.sicredi.votacao.sessao.SessaoRepository;
import br.com.sicredi.votacao.sessao.SessaoVotacao;
import br.com.sicredi.votacao.voto.OpcaoVoto;
import br.com.sicredi.votacao.voto.Voto;
import br.com.sicredi.votacao.voto.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResultadoPerformanceIntegrationTest {

    private static final int TOTAL_VOTOS = 10_000;
    private static final int VOTOS_SIM = 6_000;
    private static final int VOTOS_NAO = 4_000;

    @Autowired
    private MockMvc mockMvc;

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
    void shouldReturnAggregatedResultForLargeVoteVolume() {
        Pauta pauta = criarPautaComSessaoEncerrada();
        criarVotosEmVolume(pauta);

        assertTimeout(Duration.ofSeconds(5), () ->
                mockMvc.perform(get("/api/v1/pautas/{pautaId}/resultado", pauta.getId()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value("ENCERRADA"))
                        .andExpect(jsonPath("$.totalVotos").value(TOTAL_VOTOS))
                        .andExpect(jsonPath("$.votosSim").value(VOTOS_SIM))
                        .andExpect(jsonPath("$.votosNao").value(VOTOS_NAO))
                        .andExpect(jsonPath("$.resultado").value("APROVADA"))
        );
    }

    private Pauta criarPautaComSessaoEncerrada() {
        LocalDateTime openedAt = LocalDateTime.now().minusMinutes(20);
        Pauta pauta = pautaRepository.save(new Pauta(
                UUID.randomUUID(),
                "Pauta volume",
                "Cenario de contagem agregada com volume maior de votos",
                openedAt.minusMinutes(5)
        ));

        sessaoRepository.save(new SessaoVotacao(
                UUID.randomUUID(),
                pauta,
                openedAt,
                openedAt.plusMinutes(10),
                openedAt
        ));

        return pauta;
    }

    private void criarVotosEmVolume(Pauta pauta) {
        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(15);
        List<Voto> votos = new ArrayList<>(TOTAL_VOTOS);

        for (int i = 0; i < VOTOS_SIM; i++) {
            votos.add(criarVoto(pauta, i, OpcaoVoto.SIM, createdAt));
        }

        for (int i = VOTOS_SIM; i < TOTAL_VOTOS; i++) {
            votos.add(criarVoto(pauta, i, OpcaoVoto.NAO, createdAt));
        }

        votoRepository.saveAllAndFlush(votos);
    }

    private Voto criarVoto(Pauta pauta, int sequencial, OpcaoVoto opcao, LocalDateTime createdAt) {
        return new Voto(
                UUID.randomUUID(),
                pauta,
                "cpf-volume-" + sequencial,
                opcao,
                createdAt
        );
    }
}
