package br.com.sicredi.votacao.pauta;

import br.com.sicredi.votacao.pauta.dto.CriarPautaRequest;
import br.com.sicredi.votacao.pauta.dto.PautaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PautaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PautaService.class);

    private final PautaRepository pautaRepository;

    public PautaService(PautaRepository pautaRepository) {
        this.pautaRepository = pautaRepository;
    }

    @Transactional
    public PautaResponse criar(CriarPautaRequest request) {
        Pauta pauta = new Pauta(
                UUID.randomUUID(),
                request.titulo(),
                request.descricao(),
                LocalDateTime.now()
        );

        Pauta pautaSalva = pautaRepository.save(pauta);
        LOGGER.info("Pauta criada: pautaId={}", pautaSalva.getId());
        return PautaResponse.from(pautaSalva);
    }
}
