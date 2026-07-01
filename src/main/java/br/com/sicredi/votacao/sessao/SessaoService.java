package br.com.sicredi.votacao.sessao;

import br.com.sicredi.votacao.pauta.Pauta;
import br.com.sicredi.votacao.pauta.PautaRepository;
import br.com.sicredi.votacao.sessao.dto.AbrirSessaoRequest;
import br.com.sicredi.votacao.sessao.dto.SessaoResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessaoService {

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pauta nao encontrada"));

        if (sessaoRepository.existsByPautaId(pautaId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sessao ja aberta para esta pauta");
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
            return SessaoResponse.from(sessaoRepository.save(sessao));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sessao ja aberta para esta pauta", exception);
        }
    }
}
