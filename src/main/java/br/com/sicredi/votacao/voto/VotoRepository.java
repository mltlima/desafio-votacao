package br.com.sicredi.votacao.voto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface VotoRepository extends JpaRepository<Voto, UUID> {

    boolean existsByPauta_IdAndAssociadoId(UUID pautaId, String associadoId);

    @Query("""
            select new br.com.sicredi.votacao.voto.ContagemVotos(
                coalesce(sum(case when v.opcao = :sim then 1 else 0 end), 0),
                coalesce(sum(case when v.opcao = :nao then 1 else 0 end), 0),
                count(v)
            )
            from Voto v
            where v.pauta.id = :pautaId
            """)
    ContagemVotos contarVotosPorPauta(
            @Param("pautaId") UUID pautaId,
            @Param("sim") OpcaoVoto sim,
            @Param("nao") OpcaoVoto nao
    );
}
