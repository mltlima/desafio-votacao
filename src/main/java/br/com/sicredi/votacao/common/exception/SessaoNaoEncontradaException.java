package br.com.sicredi.votacao.common.exception;

public class SessaoNaoEncontradaException extends RuntimeException {

    public SessaoNaoEncontradaException() {
        super("Sessao de votacao nao aberta");
    }
}
