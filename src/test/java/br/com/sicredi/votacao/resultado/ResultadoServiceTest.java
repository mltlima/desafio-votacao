package br.com.sicredi.votacao.resultado;

import br.com.sicredi.votacao.common.exception.PautaNaoEncontradaException;
import br.com.sicredi.votacao.pauta.Pauta;
import br.com.sicredi.votacao.pauta.PautaRepository;
import br.com.sicredi.votacao.resultado.dto.ResultadoResponse;
import br.com.sicredi.votacao.sessao.SessaoRepository;
import br.com.sicredi.votacao.sessao.SessaoVotacao;
import br.com.sicredi.votacao.voto.ContagemVotos;
import br.com.sicredi.votacao.voto.OpcaoVoto;
import br.com.sicredi.votacao.voto.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultadoServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-30T21:45:00Z"),
            ZoneId.of("UTC")
    );

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private VotoRepository votoRepository;

    private ResultadoService resultadoService;

    @BeforeEach
    void setUp() {
        resultadoService = new ResultadoService(pautaRepository, sessaoRepository, votoRepository, FIXED_CLOCK);
    }

    @Test
    void deveRetornarNotFoundQuandoPautaNaoExistir() {
        UUID pautaId = UUID.randomUUID();
        when(pautaRepository.existsById(pautaId)).thenReturn(false);

        assertThatThrownBy(() -> resultadoService.consultar(pautaId))
                .isInstanceOf(PautaNaoEncontradaException.class)
                .hasMessage("Pauta nao encontrada");
    }

    @Test
    void deveRetornarNaoIniciadaQuandoNaoExistirSessao() {
        UUID pautaId = UUID.randomUUID();
        when(pautaRepository.existsById(pautaId)).thenReturn(true);
        when(votoRepository.contarVotosPorPauta(pautaId, OpcaoVoto.SIM, OpcaoVoto.NAO))
                .thenReturn(new ContagemVotos(0L, 0L, 0L));
        when(sessaoRepository.findByPauta_Id(pautaId)).thenReturn(Optional.empty());

        ResultadoResponse response = resultadoService.consultar(pautaId);

        assertThat(response.pautaId()).isEqualTo(pautaId);
        assertThat(response.status()).isEqualTo(StatusVotacao.NAO_INICIADA);
        assertThat(response.resultado()).isEqualTo(ResultadoVotacao.NAO_FINALIZADA);
        assertThat(response.totalVotos()).isZero();
        assertThat(response.votosSim()).isZero();
        assertThat(response.votosNao()).isZero();
        verify(votoRepository).contarVotosPorPauta(pautaId, OpcaoVoto.SIM, OpcaoVoto.NAO);
    }

    @Test
    void deveRetornarAbertaENaoFinalizadaQuandoSessaoEstiverAberta() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = criarPauta(pautaId);
        when(pautaRepository.existsById(pautaId)).thenReturn(true);
        when(votoRepository.contarVotosPorPauta(pautaId, OpcaoVoto.SIM, OpcaoVoto.NAO))
                .thenReturn(new ContagemVotos(2L, 1L, 3L));
        when(sessaoRepository.findByPauta_Id(pautaId)).thenReturn(Optional.of(criarSessaoAberta(pauta)));

        ResultadoResponse response = resultadoService.consultar(pautaId);

        assertThat(response.status()).isEqualTo(StatusVotacao.ABERTA);
        assertThat(response.resultado()).isEqualTo(ResultadoVotacao.NAO_FINALIZADA);
        assertThat(response.totalVotos()).isEqualTo(3);
        assertThat(response.votosSim()).isEqualTo(2);
        assertThat(response.votosNao()).isEqualTo(1);
    }

    @Test
    void deveRetornarAprovadaQuandoSessaoEncerradaEVotosSimForemMaioria() {
        ResultadoResponse response = consultarResultadoEncerrado(new ContagemVotos(6L, 4L, 10L));

        assertThat(response.status()).isEqualTo(StatusVotacao.ENCERRADA);
        assertThat(response.resultado()).isEqualTo(ResultadoVotacao.APROVADA);
        assertThat(response.totalVotos()).isEqualTo(10);
        assertThat(response.votosSim()).isEqualTo(6);
        assertThat(response.votosNao()).isEqualTo(4);
    }

    @Test
    void deveRetornarRejeitadaQuandoSessaoEncerradaEVotosNaoForemMaioria() {
        ResultadoResponse response = consultarResultadoEncerrado(new ContagemVotos(4L, 6L, 10L));

        assertThat(response.status()).isEqualTo(StatusVotacao.ENCERRADA);
        assertThat(response.resultado()).isEqualTo(ResultadoVotacao.REJEITADA);
    }

    @Test
    void deveRetornarEmpatadaQuandoSessaoEncerradaEVotosForemIguais() {
        ResultadoResponse response = consultarResultadoEncerrado(new ContagemVotos(5L, 5L, 10L));

        assertThat(response.status()).isEqualTo(StatusVotacao.ENCERRADA);
        assertThat(response.resultado()).isEqualTo(ResultadoVotacao.EMPATADA);
    }

    private ResultadoResponse consultarResultadoEncerrado(ContagemVotos contagem) {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = criarPauta(pautaId);
        when(pautaRepository.existsById(pautaId)).thenReturn(true);
        when(votoRepository.contarVotosPorPauta(pautaId, OpcaoVoto.SIM, OpcaoVoto.NAO)).thenReturn(contagem);
        when(sessaoRepository.findByPauta_Id(pautaId)).thenReturn(Optional.of(criarSessaoEncerrada(pauta)));

        return resultadoService.consultar(pautaId);
    }

    private Pauta criarPauta(UUID pautaId) {
        return new Pauta(
                pautaId,
                "Assembleia ordinaria",
                "Discussao anual",
                LocalDateTime.of(2026, 6, 30, 20, 0)
        );
    }

    private SessaoVotacao criarSessaoAberta(Pauta pauta) {
        return new SessaoVotacao(
                UUID.randomUUID(),
                pauta,
                LocalDateTime.of(2026, 6, 30, 21, 40),
                LocalDateTime.of(2026, 6, 30, 21, 50),
                LocalDateTime.of(2026, 6, 30, 21, 40)
        );
    }

    private SessaoVotacao criarSessaoEncerrada(Pauta pauta) {
        return new SessaoVotacao(
                UUID.randomUUID(),
                pauta,
                LocalDateTime.of(2026, 6, 30, 21, 30),
                LocalDateTime.of(2026, 6, 30, 21, 44),
                LocalDateTime.of(2026, 6, 30, 21, 30)
        );
    }
}
