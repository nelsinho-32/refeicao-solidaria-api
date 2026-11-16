package com.refeicaosolidaria.api.repository;

import com.refeicaosolidaria.api.domain.Solicitacao;
import com.refeicaosolidaria.api.domain.StatusSolicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    @Query("SELECT s FROM Solicitacao s WHERE s.instituicao.id = :idInstituicao")
    List<Solicitacao> findByInstituicaoId(@Param("idInstituicao") Long idInstituicao);

    @Query("SELECT s FROM Solicitacao s WHERE s.doacao.id = :idDoacao")
    List<Solicitacao> findByDoacaoId(@Param("idDoacao") Long idDoacao);

    @Query("SELECT s FROM Solicitacao s WHERE s.instituicao.id = :idInstituicao AND s.status = :status")
    List<Solicitacao> findByInstituicaoIdAndStatus(@Param("idInstituicao") Long idInstituicao, @Param("status") StatusSolicitacao status);

    @Query("SELECT s FROM Solicitacao s WHERE s.doacao.id = :idDoacao AND s.status = :status")
    List<Solicitacao> findByDoacaoIdAndStatus(@Param("idDoacao") Long idDoacao, @Param("status") StatusSolicitacao status);
}
