package br.com.sicredi.votacao.voto;

import br.com.sicredi.votacao.pauta.Pauta;
import br.com.sicredi.votacao.pauta.PautaRepository;
import br.com.sicredi.votacao.sessao.SessaoRepository;
import br.com.sicredi.votacao.sessao.SessaoVotacao;
import br.com.sicredi.votacao.voto.dto.RegistrarVotoRequest;
import br.com.sicredi.votacao.voto.dto.VotoResponse;
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
class VotoServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-30T21:45:00Z"),
            ZoneId.of("UTC")
    );

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private SessaoRepository sessaoRepository;

    private VotoService votoService;

    @BeforeEach
    void setUp() {
        votoService = new VotoService(votoRepository, pautaRepository, sessaoRepository, FIXED_CLOCK);
    }

    @Test
    void deveRegistrarVotoSimComSessaoAberta() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = criarPauta(pautaId);
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPauta_Id(pautaId)).thenReturn(Optional.of(criarSessaoAberta(pauta)));
        when(votoRepository.existsByPauta_IdAndAssociadoId(pautaId, "associado-123")).thenReturn(false);
        when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VotoResponse response = votoService.registrar(
                pautaId,
                new RegistrarVotoRequest("associado-123", OpcaoVoto.SIM)
        );

        ArgumentCaptor<Voto> votoCaptor = ArgumentCaptor.forClass(Voto.class);
        verify(votoRepository).save(votoCaptor.capture());

        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 30, 21, 45);
        Voto votoSalvo = votoCaptor.getValue();
        assertThat(votoSalvo.getId()).isNotNull();
        assertThat(votoSalvo.getPauta()).isEqualTo(pauta);
        assertThat(votoSalvo.getAssociadoId()).isEqualTo("associado-123");
        assertThat(votoSalvo.getOpcao()).isEqualTo(OpcaoVoto.SIM);
        assertThat(votoSalvo.getCreatedAt()).isEqualTo(createdAt);

        assertThat(response.id()).isEqualTo(votoSalvo.getId());
        assertThat(response.pautaId()).isEqualTo(pautaId);
        assertThat(response.associadoId()).isEqualTo("associado-123");
        assertThat(response.opcao()).isEqualTo(OpcaoVoto.SIM);
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void deveRegistrarVotoNaoComSessaoAberta() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = criarPauta(pautaId);
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPauta_Id(pautaId)).thenReturn(Optional.of(criarSessaoAberta(pauta)));
        when(votoRepository.existsByPauta_IdAndAssociadoId(pautaId, "associado-123")).thenReturn(false);
        when(votoRepository.save(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VotoResponse response = votoService.registrar(
                pautaId,
                new RegistrarVotoRequest("associado-123", OpcaoVoto.NAO)
        );

        assertThat(response.opcao()).isEqualTo(OpcaoVoto.NAO);
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 6, 30, 21, 45));
    }

    @Test
    void deveRetornarNotFoundQuandoPautaNaoExistir() {
        UUID pautaId = UUID.randomUUID();
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> votoService.registrar(
                pautaId,
                new RegistrarVotoRequest("associado-123", OpcaoVoto.SIM)
        )).isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    void deveRetornarConflictQuandoNaoExistirSessaoParaPauta() {
        UUID pautaId = UUID.randomUUID();
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(criarPauta(pautaId)));
        when(sessaoRepository.findByPauta_Id(pautaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> votoService.registrar(
                pautaId,
                new RegistrarVotoRequest("associado-123", OpcaoVoto.SIM)
        )).isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    void deveRetornarConflictQuandoSessaoAindaNaoAbriu() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = criarPauta(pautaId);
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPauta_Id(pautaId)).thenReturn(Optional.of(criarSessaoFutura(pauta)));

        assertThatThrownBy(() -> votoService.registrar(
                pautaId,
                new RegistrarVotoRequest("associado-123", OpcaoVoto.SIM)
        )).isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    void deveRetornarConflictQuandoSessaoJaFechou() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = criarPauta(pautaId);
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPauta_Id(pautaId)).thenReturn(Optional.of(criarSessaoFechada(pauta)));

        assertThatThrownBy(() -> votoService.registrar(
                pautaId,
                new RegistrarVotoRequest("associado-123", OpcaoVoto.SIM)
        )).isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    void deveRetornarConflictQuandoAssociadoJaVotouNaPauta() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = criarPauta(pautaId);
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPauta_Id(pautaId)).thenReturn(Optional.of(criarSessaoAberta(pauta)));
        when(votoRepository.existsByPauta_IdAndAssociadoId(pautaId, "associado-123")).thenReturn(true);

        assertThatThrownBy(() -> votoService.registrar(
                pautaId,
                new RegistrarVotoRequest("associado-123", OpcaoVoto.SIM)
        )).isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(votoRepository, never()).save(any(Voto.class));
    }

    @Test
    void deveConverterViolacaoDeConstraintUnicaEmConflict() {
        UUID pautaId = UUID.randomUUID();
        Pauta pauta = criarPauta(pautaId);
        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.findByPauta_Id(pautaId)).thenReturn(Optional.of(criarSessaoAberta(pauta)));
        when(votoRepository.existsByPauta_IdAndAssociadoId(pautaId, "associado-123")).thenReturn(false);
        when(votoRepository.save(any(Voto.class)))
                .thenThrow(new DataIntegrityViolationException("uk_votos_pauta_associado"));

        assertThatThrownBy(() -> votoService.registrar(
                pautaId,
                new RegistrarVotoRequest("associado-123", OpcaoVoto.SIM)
        )).isInstanceOfSatisfying(ResponseStatusException.class, exception ->
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

    private SessaoVotacao criarSessaoAberta(Pauta pauta) {
        return new SessaoVotacao(
                UUID.randomUUID(),
                pauta,
                LocalDateTime.of(2026, 6, 30, 21, 40),
                LocalDateTime.of(2026, 6, 30, 21, 50),
                LocalDateTime.of(2026, 6, 30, 21, 40)
        );
    }

    private SessaoVotacao criarSessaoFutura(Pauta pauta) {
        return new SessaoVotacao(
                UUID.randomUUID(),
                pauta,
                LocalDateTime.of(2026, 6, 30, 21, 46),
                LocalDateTime.of(2026, 6, 30, 21, 50),
                LocalDateTime.of(2026, 6, 30, 21, 46)
        );
    }

    private SessaoVotacao criarSessaoFechada(Pauta pauta) {
        return new SessaoVotacao(
                UUID.randomUUID(),
                pauta,
                LocalDateTime.of(2026, 6, 30, 21, 30),
                LocalDateTime.of(2026, 6, 30, 21, 44),
                LocalDateTime.of(2026, 6, 30, 21, 30)
        );
    }
}
