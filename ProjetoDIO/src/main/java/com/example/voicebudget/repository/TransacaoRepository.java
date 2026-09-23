package com.example.voicebudget.repository;

import com.example.voicebudget.model.CategoriaTransacao;
import com.example.voicebudget.model.TipoTransacao;
import com.example.voicebudget.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findAllByOrderByDataTransacaoDescCreatedAtDesc();

    List<Transacao> findTop10ByOrderByDataTransacaoDescCreatedAtDesc();

    List<Transacao> findByCategoriaOrderByDataTransacaoDesc(CategoriaTransacao categoria);

    List<Transacao> findByTipoOrderByDataTransacaoDesc(TipoTransacao tipo);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.tipo = :tipo")
    BigDecimal sumValorByTipo(@Param("tipo") TipoTransacao tipo);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.tipo = 'DESPESA' AND t.categoria = :categoria")
    BigDecimal sumDespesasByCategoria(@Param("categoria") CategoriaTransacao categoria);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t WHERE t.tipo = 'DESPESA' AND t.categoria = :categoria AND t.dataTransacao BETWEEN :inicio AND :fim")
    BigDecimal sumDespesasByCategoriaAndPeriodo(@Param("categoria") CategoriaTransacao categoria,
                                                @Param("inicio") LocalDate inicio,
                                                @Param("fim") LocalDate fim);

    @Query("SELECT t.categoria, SUM(t.valor) FROM Transacao t WHERE t.tipo = 'DESPESA' GROUP BY t.categoria")
    List<Object[]> findTotalDespesasAgrupadasPorCategoria();
}
