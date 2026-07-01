package br.com.sicredi.votacao.common.exception;

public class PautaNaoEncontradaException extends RuntimeException {

    public PautaNaoEncontradaException() {
        super("Pauta nao encontrada");
    }
}
