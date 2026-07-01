package br.com.sicredi.votacao.resultado;

import br.com.sicredi.votacao.common.exception.PautaNaoEncontradaException;
import br.com.sicredi.votacao.pauta.PautaRepository;
import br.com.sicredi.votacao.resultado.dto.ResultadoResponse;
import br.com.sicredi.votacao.sessao.SessaoRepository;
import br.com.sicredi.votacao.sessao.SessaoVotacao;
import br.com.sicredi.votacao.voto.ContagemVotos;
import br.com.sicredi.votacao.voto.OpcaoVoto;
import br.com.sicredi.votacao.voto.VotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ResultadoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultadoService.class);

    private final PautaRepository pautaRepository;
    private final SessaoRepository sessaoRepository;
    private final VotoRepository votoRepository;
    private final Clock clock;

    public ResultadoService(
            PautaRepository pautaRepository,
            SessaoRepository sessaoRepository,
            VotoRepository votoRepository,
            Clock clock
    ) {
        this.pautaRepository = pautaRepository;
        this.sessaoRepository = sessaoRepository;
        this.votoRepository = votoRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ResultadoResponse consultar(UUID pautaId) {
        if (!pautaRepository.existsById(pautaId)) {
            throw new PautaNaoEncontradaException();
        }

        ContagemVotos contagem = votoRepository.contarVotosPorPauta(pautaId, OpcaoVoto.SIM, OpcaoVoto.NAO);

        ResultadoResponse response = sessaoRepository.findByPauta_Id(pautaId)
                .map(sessao -> montarResultadoComSessao(pautaId, sessao, contagem))
                .orElseGet(() -> montarResultadoNaoIniciado(pautaId, contagem));

        LOGGER.info(
                "Resultado consultado: pautaId={}, status={}, totalVotos={}, votosSim={}, votosNao={}, resultado={}",
                pautaId,
                response.status(),
                response.totalVotos(),
                response.votosSim(),
                response.votosNao(),
                response.resultado()
        );

        return response;
    }

    private ResultadoResponse montarResultadoComSessao(UUID pautaId, SessaoVotacao sessao, ContagemVotos contagem) {
        LocalDateTime now = LocalDateTime.now(clock);
        boolean aberta = !now.isBefore(sessao.getOpenedAt()) && now.isBefore(sessao.getClosesAt());

        if (aberta) {
            return montarResponse(pautaId, StatusVotacao.ABERTA, contagem, ResultadoVotacao.NAO_FINALIZADA);
        }
        if (now.isBefore(sessao.getOpenedAt())) {
            return montarResponse(pautaId, StatusVotacao.NAO_INICIADA, contagem, ResultadoVotacao.NAO_FINALIZADA);
        }

        return montarResponse(pautaId, StatusVotacao.ENCERRADA, contagem, calcularResultadoFinal(contagem));
    }

    private ResultadoResponse montarResultadoNaoIniciado(UUID pautaId, ContagemVotos contagem) {
        return montarResponse(pautaId, StatusVotacao.NAO_INICIADA, contagem, ResultadoVotacao.NAO_FINALIZADA);
    }

    private ResultadoResponse montarResponse(
            UUID pautaId,
            StatusVotacao status,
            ContagemVotos contagem,
            ResultadoVotacao resultado
    ) {
        return new ResultadoResponse(
                pautaId,
                status,
                contagem.totalVotos(),
                contagem.votosSim(),
                contagem.votosNao(),
                resultado
        );
    }

    private ResultadoVotacao calcularResultadoFinal(ContagemVotos contagem) {
        if (contagem.votosSim() > contagem.votosNao()) {
            return ResultadoVotacao.APROVADA;
        }
        if (contagem.votosNao() > contagem.votosSim()) {
            return ResultadoVotacao.REJEITADA;
        }
        return ResultadoVotacao.EMPATADA;
    }
}
