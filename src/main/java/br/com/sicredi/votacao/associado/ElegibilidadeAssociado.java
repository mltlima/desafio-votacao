package br.com.sicredi.votacao.associado;

public record ElegibilidadeAssociado(
        boolean cpfValido,
        StatusElegibilidade status
) {

    public static ElegibilidadeAssociado cpfInvalido() {
        return new ElegibilidadeAssociado(false, null);
    }

    public static ElegibilidadeAssociado apto() {
        return new ElegibilidadeAssociado(true, StatusElegibilidade.ABLE_TO_VOTE);
    }

    public static ElegibilidadeAssociado inapto() {
        return new ElegibilidadeAssociado(true, StatusElegibilidade.UNABLE_TO_VOTE);
    }
}
