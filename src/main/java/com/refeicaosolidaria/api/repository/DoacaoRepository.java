package com.refeicaosolidaria.api.repository;

import com.refeicaosolidaria.api.domain.Doacao;
import com.refeicaosolidaria.api.domain.StatusDoacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    List<Doacao> findByStatus(StatusDoacao status);

    @Query("SELECT d FROM Doacao d WHERE d.doador.id = :idDoador")
    List<Doacao> findByDoadorId(@Param("idDoador") Long idDoador);

    @Query("SELECT d FROM Doacao d WHERE d.doador.id = :idDoador AND d.status = :status")
    List<Doacao> findByDoadorIdAndStatus(@Param("idDoador") Long idDoador, @Param("status") StatusDoacao status);
}
