package com.example.voicebudget;

import com.example.voicebudget.dto.VoiceProcessResponseDTO;
import com.example.voicebudget.model.RegistroAuditoria;
import com.example.voicebudget.service.AuditoriaService;
import com.example.voicebudget.service.ai.BudgetAssistantService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BudgetAssistantTest {

    @Autowired
    private BudgetAssistantService assistantService;

    @Autowired
    private AuditoriaService auditoriaService;

    @Test
    @DisplayName("Deve processar comando de registro de despesa e gravar auditoria")
    void deveProcessarComandoEGravarAuditoria() {
        String comando = "Gastei 65 reais na farmácia comprando vitaminas";

        VoiceProcessResponseDTO response = assistantService.processarTexto(comando);

        assertNotNull(response);
        assertTrue(response.sucesso());
        assertNotNull(response.respostaIA());
        assertFalse(response.acoesExecutadas().isEmpty());

        // Verifica se a auditoria registrou o comando
        List<RegistroAuditoria> logs = auditoriaService.listarRecentes();
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(l -> l.getEntradaUsuario().contains("farmácia")));
    }

    @Test
    @DisplayName("Deve processar consulta de saldo via linguagem natural")
    void deveConsultarSaldoViaTexto() {
        String comando = "Qual é o meu saldo atual?";

        VoiceProcessResponseDTO response = assistantService.processarTexto(comando);

        assertNotNull(response);
        assertTrue(response.sucesso());
        assertTrue(response.respostaIA().toLowerCase().contains("saldo"));
    }
}
