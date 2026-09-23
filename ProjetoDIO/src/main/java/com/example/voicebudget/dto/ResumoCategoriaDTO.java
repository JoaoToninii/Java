package com.example.voicebudget.dto;

import com.example.voicebudget.model.CategoriaTransacao;
import java.math.BigDecimal;

public record ResumoCategoriaDTO(
        CategoriaTransacao categoria,
        BigDecimal totalGasto,
        BigDecimal limiteMensal,
        Double porcentagemUtilizada,
        String alerta
) {}
