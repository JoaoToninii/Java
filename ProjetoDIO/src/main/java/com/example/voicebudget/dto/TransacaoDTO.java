package com.example.voicebudget.dto;

import com.example.voicebudget.model.CategoriaTransacao;
import com.example.voicebudget.model.TipoTransacao;
import com.example.voicebudget.model.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransacaoDTO(
        Long id,
        TipoTransacao tipo,
        BigDecimal valor,
        CategoriaTransacao categoria,
        String descricao,
        LocalDate dataTransacao,
        LocalDateTime createdAt
) {
    public static TransacaoDTO fromEntity(Transacao entity) {
        return new TransacaoDTO(
                entity.getId(),
                entity.getTipo(),
                entity.getValor(),
                entity.getCategoria(),
                entity.getDescricao(),
                entity.getDataTransacao(),
                entity.getCreatedAt()
        );
    }
}
