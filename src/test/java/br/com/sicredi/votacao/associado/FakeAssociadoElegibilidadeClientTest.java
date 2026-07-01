package br.com.sicredi.votacao.associado;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FakeAssociadoElegibilidadeClientTest {

    @Test
    void deveRetornarAbleToVoteParaCpfNaoMapeadoQuandoRandomEstiverDesabilitado() {
        AssociadoElegibilidadeProperties properties = new AssociadoElegibilidadeProperties();
        properties.setRandomEnabled(false);
        FakeAssociadoElegibilidadeClient client = new FakeAssociadoElegibilidadeClient(properties);

        ElegibilidadeAssociado elegibilidade = client.consultar("99999999999");

        assertThat(elegibilidade.cpfValido()).isTrue();
        assertThat(elegibilidade.status()).isEqualTo(StatusElegibilidade.ABLE_TO_VOTE);
    }

    @Test
    void deveRespeitarListasConfiguradasAntesDoDefaultDeterministico() {
        AssociadoElegibilidadeProperties properties = new AssociadoElegibilidadeProperties();
        properties.setRandomEnabled(false);
        properties.setCpfsInvalidos(List.of("00000000000"));
        properties.setCpfsUnable(List.of("11111111111"));
        properties.setCpfsAble(List.of("22222222222"));
        FakeAssociadoElegibilidadeClient client = new FakeAssociadoElegibilidadeClient(properties);

        ElegibilidadeAssociado cpfInvalido = client.consultar("00000000000");
        ElegibilidadeAssociado associadoInapto = client.consultar("11111111111");
        ElegibilidadeAssociado associadoApto = client.consultar("22222222222");

        assertThat(cpfInvalido.cpfValido()).isFalse();
        assertThat(cpfInvalido.status()).isNull();
        assertThat(associadoInapto.cpfValido()).isTrue();
        assertThat(associadoInapto.status()).isEqualTo(StatusElegibilidade.UNABLE_TO_VOTE);
        assertThat(associadoApto.cpfValido()).isTrue();
        assertThat(associadoApto.status()).isEqualTo(StatusElegibilidade.ABLE_TO_VOTE);
    }

    @Test
    void deveRetornarRespostaValidaQuandoRandomEstiverHabilitado() {
        AssociadoElegibilidadeProperties properties = new AssociadoElegibilidadeProperties();
        properties.setRandomEnabled(true);
        FakeAssociadoElegibilidadeClient client = new FakeAssociadoElegibilidadeClient(properties);

        ElegibilidadeAssociado elegibilidade = client.consultar("99999999999");

        if (elegibilidade.cpfValido()) {
            assertThat(elegibilidade.status()).isIn(StatusElegibilidade.ABLE_TO_VOTE, StatusElegibilidade.UNABLE_TO_VOTE);
        } else {
            assertThat(elegibilidade.status()).isNull();
        }
    }
}
