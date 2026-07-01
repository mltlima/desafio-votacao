package br.com.sicredi.votacao.common.exception;

public class SessaoFechadaException extends RuntimeException {

    public SessaoFechadaException() {
        super("Sessao de votacao nao esta aberta");
    }
}
