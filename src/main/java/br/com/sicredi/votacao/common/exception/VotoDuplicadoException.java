package br.com.sicredi.votacao.common.exception;

public class VotoDuplicadoException extends RuntimeException {

    public VotoDuplicadoException() {
        super("Associado ja votou nesta pauta");
    }

    public VotoDuplicadoException(Throwable cause) {
        super("Associado ja votou nesta pauta", cause);
    }
}
