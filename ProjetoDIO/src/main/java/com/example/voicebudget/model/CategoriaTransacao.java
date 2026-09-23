package com.example.voicebudget.model;

public enum CategoriaTransacao {
    ALIMENTACAO,
    TRANSPORTE,
    MORADIA,
    LAZER,
    SALARIO,
    SAUDE,
    EDUCACAO,
    OUTROS;

    public static CategoriaTransacao fromString(String valor) {
        if (valor == null || valor.isBlank()) {
            return OUTROS;
        }
        String normalizado = valor.trim().toUpperCase()
                .replace("Ã", "A").replace("Á", "A").replace("À", "A")
                .replace("É", "E").replace("Ê", "E")
                .replace("Í", "I")
                .replace("Ó", "O").replace("Õ", "O").replace("Ô", "O")
                .replace("Ú", "U")
                .replace("Ç", "C");

        if (normalizado.contains("ALIMENT") || normalizado.contains("COMIDA") || normalizado.contains("ALMO") || normalizado.contains("JANT") || normalizado.contains("MERCAD") || normalizado.contains("LANCH") || normalizado.contains("RESTAUR")) {
            return ALIMENTACAO;
        }
        if (normalizado.contains("TRANSP") || normalizado.contains("UBER") || normalizado.contains("GASOLIN") || normalizado.contains("COMBUST") || normalizado.contains("ONIBUS") || normalizado.contains("METRO")) {
            return TRANSPORTE;
        }
        if (normalizado.contains("MORAD") || normalizado.contains("ALUGUEL") || normalizado.contains("CONDOM") || normalizado.contains("LUZ") || normalizado.contains("AGUA") || normalizado.contains("INTERNET")) {
            return MORADIA;
        }
        if (normalizado.contains("LAZER") || normalizado.contains("CINEMA") || normalizado.contains("FESTA") || normalizado.contains("VIAG") || normalizado.contains("SHOW") || normalizado.contains("BAR")) {
            return LAZER;
        }
        if (normalizado.contains("SALAR") || normalizado.contains("PAGAMENT") || normalizado.contains("PROVENT") || normalizado.contains("REND")) {
            return SALARIO;
        }
        if (normalizado.contains("SAUD") || normalizado.contains("FARMAC") || normalizado.contains("MEDIC") || normalizado.contains("REMED") || normalizado.contains("CONSULT")) {
            return SAUDE;
        }
        if (normalizado.contains("EDUC") || normalizado.contains("CURSO") || normalizado.contains("LIVRO") || normalizado.contains("FACULDADE") || normalizado.contains("ESCOLA")) {
            return EDUCACAO;
        }

        try {
            return CategoriaTransacao.valueOf(normalizado);
        } catch (IllegalArgumentException e) {
            return OUTROS;
        }
    }
}
