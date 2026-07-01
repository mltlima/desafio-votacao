package br.com.sicredi.votacao.voto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VotoRepository extends JpaRepository<Voto, UUID> {

    boolean existsByPauta_IdAndAssociadoId(UUID pautaId, String associadoId);
}
