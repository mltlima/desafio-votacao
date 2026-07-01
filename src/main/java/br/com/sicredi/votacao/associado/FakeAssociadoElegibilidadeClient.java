package br.com.sicredi.votacao.associado;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class FakeAssociadoElegibilidadeClient implements AssociadoElegibilidadeClient {

    private final AssociadoElegibilidadeProperties properties;

    public FakeAssociadoElegibilidadeClient(AssociadoElegibilidadeProperties properties) {
        this.properties = properties;
    }

    @Override
    public ElegibilidadeAssociado consultar(String cpf) {
        if (properties.getCpfsInvalidos().contains(cpf)) {
            return ElegibilidadeAssociado.cpfInvalido();
        }

        if (properties.getCpfsUnable().contains(cpf)) {
            return ElegibilidadeAssociado.inapto();
        }

        if (properties.getCpfsAble().contains(cpf)) {
            return ElegibilidadeAssociado.apto();
        }

        if (!properties.isRandomEnabled()) {
            return ElegibilidadeAssociado.apto();
        }

        if (ThreadLocalRandom.current().nextBoolean()) {
            return ElegibilidadeAssociado.cpfInvalido();
        }

        return ThreadLocalRandom.current().nextBoolean()
                ? ElegibilidadeAssociado.apto()
                : ElegibilidadeAssociado.inapto();
    }
}
