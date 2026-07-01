package br.com.sicredi.votacao.common.exception;

public class AssociadoNaoPodeVotarException extends RuntimeException {

    public AssociadoNaoPodeVotarException() {
        super("Associado nao pode votar");
    }
}
