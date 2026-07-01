package br.com.sicredi.votacao.pauta;

import br.com.sicredi.votacao.pauta.dto.CriarPautaRequest;
import br.com.sicredi.votacao.pauta.dto.PautaResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PautaServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @InjectMocks
    private PautaService pautaService;

    @Test
    void deveCriarPautaComIdECreatedAtGeradosPelaAplicacao() {
        CriarPautaRequest request = new CriarPautaRequest("Assembleia ordinaria", "Discussao anual");
        when(pautaRepository.save(any(Pauta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PautaResponse response = pautaService.criar(request);

        ArgumentCaptor<Pauta> pautaCaptor = ArgumentCaptor.forClass(Pauta.class);
        verify(pautaRepository).save(pautaCaptor.capture());

        Pauta pautaSalva = pautaCaptor.getValue();
        assertThat(pautaSalva.getId()).isNotNull();
        assertThat(pautaSalva.getCreatedAt()).isNotNull();
        assertThat(pautaSalva.getTitulo()).isEqualTo("Assembleia ordinaria");
        assertThat(pautaSalva.getDescricao()).isEqualTo("Discussao anual");

        assertThat(response.id()).isEqualTo(pautaSalva.getId());
        assertThat(response.createdAt()).isEqualTo(pautaSalva.getCreatedAt());
        assertThat(response.titulo()).isEqualTo("Assembleia ordinaria");
        assertThat(response.descricao()).isEqualTo("Discussao anual");
    }
}
