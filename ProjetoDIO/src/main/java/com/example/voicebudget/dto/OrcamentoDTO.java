package com.example.voicebudget.dto;

import com.example.voicebudget.model.CategoriaTransacao;
import com.example.voicebudget.model.LimiteOrcamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrcamentoDTO(
        Long id,
        @NotNull(message = "A categoria é obrigatória")
        CategoriaTransacao categoria,
        @NotNull(message = "O limite mensal é obrigatório")
        @DecimalMin(value = "0.01", message = "O limite deve ser maior que zero")
        BigDecimal limiteMensal
) {
    public static OrcamentoDTO fromEntity(LimiteOrcamento entity) {
        return new OrcamentoDTO(entity.getId(), entity.getCategoria(), entity.getLimiteMensal());
    }
}
