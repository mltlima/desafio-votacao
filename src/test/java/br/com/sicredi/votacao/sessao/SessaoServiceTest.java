package br.com.sicredi.votacao.sessao;

import br.com.sicredi.votacao.pauta.Pauta;
import br.com.sicredi.votacao.pauta.PautaRepository;
import br.com.sicredi.votacao.sessao.dto.AbrirSessaoRequest;
import br.com.sicredi.votacao.sessao.dto.SessaoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessaoServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-30T21:30:00Z"),
            ZoneId.of("UTC")
    );

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private PautaRepository pautaRepository;

    private SessaoService sessaoService;

    @BeforeEach
    void setUp() {
        sessaoService = new SessaoService(sessaoRepository, pautaRepository, FIXED_CLOCK);
    }

    @Test
    void deveAbrirSessaoComDuracaoPadraoDeUmMinuto() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = criarPauta(pautaId);
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.existsByPautaId(pautaId)).thenReturn(false);
        when(sessaoRepository.save(any(SessaoVotacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessaoResponse response = sessaoService.abrir(pautaId, null);

        ArgumentCaptor<SessaoVotacao> sessaoCaptor = ArgumentCaptor.forClass(SessaoVotacao.class);
        verify(sessaoRepository).save(sessaoCaptor.capture());

        LocalDateTime openedAt = LocalDateTime.of(2026, 6, 30, 21, 30);
        SessaoVotacao sessaoSalva = sessaoCaptor.getValue();
        assertThat(sessaoSalva.getId()).isNotNull();
        assertThat(sessaoSalva.getPauta()).isEqualTo(pauta);
        assertThat(sessaoSalva.getOpenedAt()).isEqualTo(openedAt);
        assertThat(sessaoSalva.getClosesAt()).isEqualTo(openedAt.plusMinutes(1));
        assertThat(sessaoSalva.getCreatedAt()).isEqualTo(openedAt);

        assertThat(response.id()).isEqualTo(sessaoSalva.getId());
        assertThat(response.pautaId()).isEqualTo(pautaId);
        assertThat(response.openedAt()).isEqualTo(openedAt);
        assertThat(response.closesAt()).isEqualTo(openedAt.plusMinutes(1));
        assertThat(response.createdAt()).isEqualTo(openedAt);
    }

    @Test
    void deveAbrirSessaoComDuracaoCustomizada() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = criarPauta(pautaId);
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.existsByPautaId(pautaId)).thenReturn(false);
        when(sessaoRepository.save(any(SessaoVotacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SessaoResponse response = sessaoService.abrir(pautaId, new AbrirSessaoRequest(10));

        LocalDateTime openedAt = LocalDateTime.of(2026, 6, 30, 21, 30);
        assertThat(response.openedAt()).isEqualTo(openedAt);
        assertThat(response.closesAt()).isEqualTo(openedAt.plusMinutes(10));
        assertThat(response.createdAt()).isEqualTo(openedAt);
    }

    @Test
    void deveRetornarNotFoundQuandoPautaNaoExistir() {
        UUID pautaId = UUID.randomUUID();
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessaoService.abrir(pautaId, new AbrirSessaoRequest(10)))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(sessaoRepository, never()).save(any(SessaoVotacao.class));
    }

    @Test
    void deveRetornarConflictQuandoSessaoJaExistirParaPauta() {
        UUID pautaId = UUID.randomUUID();
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(criarPauta(pautaId)));
        when(sessaoRepository.existsByPautaId(pautaId)).thenReturn(true);

        assertThatThrownBy(() -> sessaoService.abrir(pautaId, new AbrirSessaoRequest(10)))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(sessaoRepository, never()).save(any(SessaoVotacao.class));
    }

    @Test
    void deveConverterViolacaoDeConstraintUnicaEmConflict() {
        UUID pautaId = UUID.randomUUID();
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(criarPauta(pautaId)));
        when(sessaoRepository.existsByPautaId(pautaId)).thenReturn(false);
        when(sessaoRepository.save(any(SessaoVotacao.class)))
                .thenThrow(new DataIntegrityViolationException("uk_sessoes_votacao_pauta"));

        assertThatThrownBy(() -> sessaoService.abrir(pautaId, new AbrirSessaoRequest(10)))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    private Pauta criarPauta(UUID pautaId) {
        return new Pauta(
                pautaId,
                "Assembleia ordinaria",
                "Discussao anual",
                LocalDateTime.of(2026, 6, 30, 20, 0)
        );
    }
}
