package br.com.sicredi.votacao.common.exception;

public class CpfInvalidoException extends RuntimeException {

    public CpfInvalidoException() {
        super("CPF invalido");
    }
}
