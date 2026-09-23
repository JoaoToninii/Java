package com.example.voicebudget.dto;

import java.util.List;

public record VoiceProcessResponseDTO(
        String canal,
        String textoComando,
        String respostaIA,
        String audioBase64,
        List<String> acoesExecutadas,
        SaldoDTO saldoAtualizado,
        Long tempoProcessamentoMs,
        boolean sucesso
) {}
