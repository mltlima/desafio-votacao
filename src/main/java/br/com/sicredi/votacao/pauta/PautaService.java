package br.com.sicredi.votacao.pauta;

import br.com.sicredi.votacao.pauta.dto.CriarPautaRequest;
import br.com.sicredi.votacao.pauta.dto.PautaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PautaService {

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

        return PautaResponse.from(pautaRepository.save(pauta));
    }
}
