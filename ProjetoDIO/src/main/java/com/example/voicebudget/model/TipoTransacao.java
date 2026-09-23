package com.example.voicebudget.model;

public enum TipoTransacao {
    RECEITA,
    DESPESA;

    public static TipoTransacao fromString(String valor) {
        if (valor == null) {
            return DESPESA;
        }
        String normalizado = valor.trim().toUpperCase();
        if (normalizado.contains("RECEIT") || normalizado.contains("ENTRAD") || normalizado.contains("GANH") || normalizado.contains("SALARI")) {
            return RECEITA;
        }
        return DESPESA;
    }
}
