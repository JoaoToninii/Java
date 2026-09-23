package com.example.voicebudget.controller;

import com.example.voicebudget.dto.ChatRequestDTO;
import com.example.voicebudget.dto.VoiceProcessResponseDTO;
import com.example.voicebudget.service.ai.AudioSpeechService;
import com.example.voicebudget.service.ai.BudgetAssistantService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/voice")
@CrossOrigin(origins = "*")
public class VoiceBudgetController {

    private final BudgetAssistantService assistantService;
    private final AudioSpeechService speechService;

    public VoiceBudgetController(BudgetAssistantService assistantService, AudioSpeechService speechService) {
        this.assistantService = assistantService;
        this.speechService = speechService;
    }

    /**
     * Endpoint principal para receber arquivo de áudio enviado pelo cliente.
     * Fluxo: Áudio -> Whisper STT -> Intenção IA -> Tool Calling -> Saldo/Transações -> Resposta + TTS.
     */
    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceProcessResponseDTO> processarVoz(
            @RequestParam("audio") MultipartFile audioFile) {
        VoiceProcessResponseDTO response = assistantService.processarAudio(audioFile);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint alternativo para envio de comandos via texto (JSON).
     * Permite testar o assistente e o Tool Calling sem necessidade de microfone.
     */
    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VoiceProcessResponseDTO> processarTexto(
            @RequestBody ChatRequestDTO request) {
        if (request == null || request.mensagem() == null || request.mensagem().isBlank()) {
            return ResponseEntity.badRequest().body(null);
        }
        VoiceProcessResponseDTO response = assistantService.processarTexto(request.mensagem());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para sintetizar texto em áudio sob demanda (TTS).
     */
    @PostMapping(value = "/tts", produces = "audio/mpeg")
    public ResponseEntity<byte[]> sintetizarVoz(
            @RequestParam("texto") String texto) {
        byte[] audioBytes = speechService.sintetizarVozBytes(texto);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"response.mp3\"")
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(audioBytes);
    }
}
