package com.example.voicebudget.dto;

import java.math.BigDecimal;

public record SaldoDTO(
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldoAtual,
        String status
) {
    public static SaldoDTO of(BigDecimal receitas, BigDecimal despesas) {
        BigDecimal r = receitas != null ? receitas : BigDecimal.ZERO;
        BigDecimal d = despesas != null ? despesas : BigDecimal.ZERO;
        BigDecimal saldo = r.subtract(d);
        String status = saldo.compareTo(BigDecimal.ZERO) >= 0 ? "POSITIVO" : "NEGATIVO";
        return new SaldoDTO(r, d, saldo, status);
    }
}
