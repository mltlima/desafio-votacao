package br.com.sicredi.votacao.voto;

import br.com.sicredi.votacao.associado.AssociadoElegibilidadeClient;
import br.com.sicredi.votacao.associado.ElegibilidadeAssociado;
import br.com.sicredi.votacao.associado.StatusElegibilidade;
import br.com.sicredi.votacao.common.exception.AssociadoNaoPodeVotarException;
import br.com.sicredi.votacao.common.exception.CpfInvalidoException;
import br.com.sicredi.votacao.common.exception.PautaNaoEncontradaException;
import br.com.sicredi.votacao.common.exception.SessaoFechadaException;
import br.com.sicredi.votacao.common.exception.SessaoNaoEncontradaException;
import br.com.sicredi.votacao.common.exception.VotoDuplicadoException;
import br.com.sicredi.votacao.pauta.Pauta;
import br.com.sicredi.votacao.pauta.PautaRepository;
import br.com.sicredi.votacao.sessao.SessaoRepository;
import br.com.sicredi.votacao.sessao.SessaoVotacao;
import br.com.sicredi.votacao.voto.dto.RegistrarVotoRequest;
import br.com.sicredi.votacao.voto.dto.VotoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VotoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VotoService.class);

    private final VotoRepository votoRepository;
    private final PautaRepository pautaRepository;
    private final SessaoRepository sessaoRepository;
    private final AssociadoElegibilidadeClient associadoElegibilidadeClient;
    private final Clock clock;

    public VotoService(
            VotoRepository votoRepository,
            PautaRepository pautaRepository,
            SessaoRepository sessaoRepository,
            AssociadoElegibilidadeClient associadoElegibilidadeClient,
            Clock clock
    ) {
        this.votoRepository = votoRepository;
        this.pautaRepository = pautaRepository;
        this.sessaoRepository = sessaoRepository;
        this.associadoElegibilidadeClient = associadoElegibilidadeClient;
        this.clock = clock;
    }

    @Transactional
    public VotoResponse registrar(UUID pautaId, RegistrarVotoRequest request) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(PautaNaoEncontradaException::new);

        SessaoVotacao sessao = sessaoRepository.findByPauta_Id(pautaId)
                .orElseThrow(() -> {
                    LOGGER.warn("Tentativa de votar sem sessao aberta: pautaId={}", pautaId);
                    return new SessaoNaoEncontradaException();
                });

        LocalDateTime now = LocalDateTime.now(clock);
        if (now.isBefore(sessao.getOpenedAt()) || !now.isBefore(sessao.getClosesAt())) {
            LOGGER.warn(
                    "Tentativa de votar fora do periodo: pautaId={}, associadoId={}, now={}, openedAt={}, closesAt={}",
                    pautaId,
                    request.associadoId(),
                    now,
                    sessao.getOpenedAt(),
                    sessao.getClosesAt()
            );
            throw new SessaoFechadaException();
        }

        if (votoRepository.existsByPauta_IdAndAssociadoId(pautaId, request.associadoId())) {
            LOGGER.warn("Tentativa de voto duplicado: pautaId={}, associadoId={}", pautaId, request.associadoId());
            throw new VotoDuplicadoException();
        }

        ElegibilidadeAssociado elegibilidade = associadoElegibilidadeClient.consultar(request.associadoId());
        if (!elegibilidade.cpfValido()) {
            LOGGER.warn("Tentativa de voto com CPF invalido: pautaId={}, associadoId={}", pautaId, request.associadoId());
            throw new CpfInvalidoException();
        }

        if (elegibilidade.status() != StatusElegibilidade.ABLE_TO_VOTE) {
            LOGGER.warn("Associado nao pode votar: pautaId={}, associadoId={}", pautaId, request.associadoId());
            throw new AssociadoNaoPodeVotarException();
        }

        Voto voto = new Voto(
                UUID.randomUUID(),
                pauta,
                request.associadoId(),
                request.opcao(),
                now
        );

        try {
            Voto votoSalvo = votoRepository.save(voto);
            LOGGER.info(
                    "Voto registrado: votoId={}, pautaId={}, associadoId={}, opcao={}",
                    votoSalvo.getId(),
                    pautaId,
                    votoSalvo.getAssociadoId(),
                    votoSalvo.getOpcao()
            );
            return VotoResponse.from(votoSalvo);
        } catch (DataIntegrityViolationException exception) {
            LOGGER.warn("Violacao de constraint ao registrar voto: pautaId={}, associadoId={}", pautaId, request.associadoId());
            throw new VotoDuplicadoException(exception);
        }
    }
}
