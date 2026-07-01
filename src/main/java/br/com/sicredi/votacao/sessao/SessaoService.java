package br.com.sicredi.votacao.sessao;

import br.com.sicredi.votacao.common.exception.PautaNaoEncontradaException;
import br.com.sicredi.votacao.common.exception.SessaoJaAbertaException;
import br.com.sicredi.votacao.pauta.Pauta;
import br.com.sicredi.votacao.pauta.PautaRepository;
import br.com.sicredi.votacao.sessao.dto.AbrirSessaoRequest;
import br.com.sicredi.votacao.sessao.dto.SessaoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessaoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessaoService.class);
    private static final int DURACAO_PADRAO_EM_MINUTOS = 1;

    private final SessaoRepository sessaoRepository;
    private final PautaRepository pautaRepository;
    private final Clock clock;

    public SessaoService(SessaoRepository sessaoRepository, PautaRepository pautaRepository, Clock clock) {
        this.sessaoRepository = sessaoRepository;
        this.pautaRepository = pautaRepository;
        this.clock = clock;
    }

    @Transactional
    public SessaoResponse abrir(UUID pautaId, AbrirSessaoRequest request) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(PautaNaoEncontradaException::new);

        if (sessaoRepository.existsByPauta_Id(pautaId)) {
            LOGGER.warn("Tentativa de abrir sessao duplicada: pautaId={}", pautaId);
            throw new SessaoJaAbertaException();
        }

        int duracaoEmMinutos = request == null || request.duracaoEmMinutos() == null
                ? DURACAO_PADRAO_EM_MINUTOS
                : request.duracaoEmMinutos();

        LocalDateTime now = LocalDateTime.now(clock);
        SessaoVotacao sessao = new SessaoVotacao(
                UUID.randomUUID(),
                pauta,
                now,
                now.plusMinutes(duracaoEmMinutos),
                now
        );

        try {
            SessaoVotacao sessaoSalva = sessaoRepository.save(sessao);
            LOGGER.info(
                    "Sessao aberta: sessaoId={}, pautaId={}, openedAt={}, closesAt={}",
                    sessaoSalva.getId(),
                    pautaId,
                    sessaoSalva.getOpenedAt(),
                    sessaoSalva.getClosesAt()
            );
            return SessaoResponse.from(sessaoSalva);
        } catch (DataIntegrityViolationException exception) {
            LOGGER.warn("Violacao de constraint ao abrir sessao: pautaId={}", pautaId);
            throw new SessaoJaAbertaException(exception);
        }
    }
}
