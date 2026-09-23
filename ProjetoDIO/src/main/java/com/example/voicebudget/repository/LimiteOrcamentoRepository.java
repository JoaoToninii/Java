package com.example.voicebudget.repository;

import com.example.voicebudget.model.CategoriaTransacao;
import com.example.voicebudget.model.LimiteOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LimiteOrcamentoRepository extends JpaRepository<LimiteOrcamento, Long> {
    Optional<LimiteOrcamento> findByCategoria(CategoriaTransacao categoria);
}
