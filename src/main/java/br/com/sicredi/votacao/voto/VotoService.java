package br.com.sicredi.votacao.voto;

import br.com.sicredi.votacao.pauta.Pauta;
import br.com.sicredi.votacao.pauta.PautaRepository;
import br.com.sicredi.votacao.sessao.SessaoRepository;
import br.com.sicredi.votacao.sessao.SessaoVotacao;
import br.com.sicredi.votacao.voto.dto.RegistrarVotoRequest;
import br.com.sicredi.votacao.voto.dto.VotoResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VotoService {

    private final VotoRepository votoRepository;
    private final PautaRepository pautaRepository;
    private final SessaoRepository sessaoRepository;
    private final Clock clock;

    public VotoService(
            VotoRepository votoRepository,
            PautaRepository pautaRepository,
            SessaoRepository sessaoRepository,
            Clock clock
    ) {
        this.votoRepository = votoRepository;
        this.pautaRepository = pautaRepository;
        this.sessaoRepository = sessaoRepository;
        this.clock = clock;
    }

    @Transactional
    public VotoResponse registrar(UUID pautaId, RegistrarVotoRequest request) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pauta nao encontrada"));

        SessaoVotacao sessao = sessaoRepository.findByPauta_Id(pautaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Sessao de votacao nao aberta"));

        LocalDateTime now = LocalDateTime.now(clock);
        if (now.isBefore(sessao.getOpenedAt()) || !now.isBefore(sessao.getClosesAt())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sessao de votacao fechada");
        }

        if (votoRepository.existsByPauta_IdAndAssociadoId(pautaId, request.associadoId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Associado ja votou nesta pauta");
        }

        Voto voto = new Voto(
                UUID.randomUUID(),
                pauta,
                request.associadoId(),
                request.opcao(),
                now
        );

        try {
            return VotoResponse.from(votoRepository.save(voto));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Associado ja votou nesta pauta", exception);
        }
    }
}
