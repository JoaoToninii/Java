package com.example.voicebudget.service;

import com.example.voicebudget.dto.OrcamentoDTO;
import com.example.voicebudget.model.CategoriaTransacao;
import com.example.voicebudget.model.LimiteOrcamento;
import com.example.voicebudget.repository.LimiteOrcamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class OrcamentoService {

    private final LimiteOrcamentoRepository limiteRepository;

    public OrcamentoService(LimiteOrcamentoRepository limiteRepository) {
        this.limiteRepository = limiteRepository;
    }

    @Transactional
    public LimiteOrcamento definirLimite(CategoriaTransacao categoria, BigDecimal valorLimite) {
        if (valorLimite == null || valorLimite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O limite de orçamento deve ser maior que zero.");
        }
        LimiteOrcamento limite = limiteRepository.findByCategoria(categoria)
                .orElse(new LimiteOrcamento(categoria, valorLimite));

        limite.setLimiteMensal(valorLimite);
        return limiteRepository.save(limite);
    }

    @Transactional(readOnly = true)
    public Optional<LimiteOrcamento> obterLimite(CategoriaTransacao categoria) {
        return limiteRepository.findByCategoria(categoria);
    }

    @Transactional(readOnly = true)
    public List<OrcamentoDTO> listarTodos() {
        return limiteRepository.findAll().stream()
                .map(OrcamentoDTO::fromEntity)
                .toList();
    }

    public String verificarAlerta(CategoriaTransacao categoria, BigDecimal totalGasto, BigDecimal limiteMensal) {
        if (limiteMensal == null || limiteMensal.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal percentual = totalGasto.divide(limiteMensal, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (totalGasto.compareTo(limiteMensal) > 0) {
            BigDecimal excedente = totalGasto.subtract(limiteMensal);
            return String.format("⚠️ ATENÇÃO: Você ultrapassou o orçamento de %s em R$ %.2f (%.1f%% do limite)!",
                    categoria.name(), excedente, percentual.doubleValue());
        } else if (percentual.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return String.format("⚠️ ALERTA: Você já consumiu %.1f%% do seu orçamento de %s (R$ %.2f de R$ %.2f)!",
                    percentual.doubleValue(), categoria.name(), totalGasto, limiteMensal);
        }
        return null;
    }
}
