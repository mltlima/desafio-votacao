package br.com.sicredi.votacao.sessao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessaoRepository extends JpaRepository<SessaoVotacao, UUID> {

    boolean existsByPauta_Id(UUID pautaId);

    Optional<SessaoVotacao> findByPauta_Id(UUID pautaId);
}
