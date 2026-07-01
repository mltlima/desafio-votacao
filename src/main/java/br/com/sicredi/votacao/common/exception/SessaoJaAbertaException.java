package br.com.sicredi.votacao.common.exception;

public class SessaoJaAbertaException extends RuntimeException {

    public SessaoJaAbertaException() {
        super("Sessao ja aberta para esta pauta");
    }

    public SessaoJaAbertaException(Throwable cause) {
        super("Sessao ja aberta para esta pauta", cause);
    }
}
