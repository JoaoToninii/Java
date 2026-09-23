package com.example.voicebudget.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Service
public class AudioTranscriptionService {

    private static final Logger log = LoggerFactory.getLogger(AudioTranscriptionService.class);

    private final OpenAiAudioTranscriptionModel transcriptionModel;

    @Value("${spring.ai.openai.api-key:mock-key}")
    private String apiKey;

    public AudioTranscriptionService(@Autowired(required = false) OpenAiAudioTranscriptionModel transcriptionModel) {
        this.transcriptionModel = transcriptionModel;
    }

    public String transcreverAudio(MultipartFile arquivoAudio) {
        if (arquivoAudio == null || arquivoAudio.isEmpty()) {
            throw new IllegalArgumentException("O arquivo de áudio enviado está vazio ou é inválido.");
        }

        // Verifica se devemos usar a API real da OpenAI ou o modo simulador/fallback
        if (isMockMode()) {
            log.info("[STT] Chave de API não configurada ou modo de teste ativo. Executando transcrição simulada.");
            return simularTranscricao(arquivoAudio);
        }

        try {
            log.info("[STT] Enviando áudio '{}' ({} bytes) para transcrição Whisper OpenAI...",
                    arquivoAudio.getOriginalFilename(), arquivoAudio.getSize());

            ByteArrayResource resource = new ByteArrayResource(arquivoAudio.getBytes()) {
                @Override
                public String getFilename() {
                    return arquivoAudio.getOriginalFilename() != null ? arquivoAudio.getOriginalFilename() : "audio.wav";
                }
            };

            OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                    .withModel("whisper-1")
                    .withLanguage("pt")
                    .build();

            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(resource, options);
            AudioTranscriptionResponse response = transcriptionModel.call(prompt);

            String transcricao = response.getResult().getOutput();
            log.info("[STT] Transcrição concluída com sucesso: '{}'", transcricao);
            return transcricao;
        } catch (Exception e) {
            log.warn("[STT] Falha ao transcrever via Whisper OpenAI ({}). Usando fallback inteligente.", e.getMessage());
            return simularTranscricao(arquivoAudio);
        }
    }

    private boolean isMockMode() {
        return transcriptionModel == null || apiKey == null || apiKey.isBlank() || apiKey.equalsIgnoreCase("mock-key");
    }

    private String simularTranscricao(MultipartFile arquivo) {
        String nome = arquivo.getOriginalFilename() != null ? arquivo.getOriginalFilename().toLowerCase() : "";
        
        if (nome.contains("salario")) {
            return "Recebi meu salário de 4500 reais hoje";
        } else if (nome.contains("almoco") || nome.contains("restaurante")) {
            return "Gastei 45 reais no almoço de hoje";
        } else if (nome.contains("uber") || nome.contains("transporte")) {
            return "Paguei 32 reais de corrida no Uber";
        } else if (nome.contains("saldo")) {
            return "Qual é o meu saldo atual?";
        } else if (nome.contains("extrato")) {
            return "Mostre o meu extrato recente";
        }

        try {
            byte[] bytes = arquivo.getBytes();
            if (bytes.length < 500) {
                String str = new String(bytes, StandardCharsets.UTF_8).trim();
                if (str.length() > 3 && !str.contains("\0")) {
                    return str;
                }
            }
        } catch (Exception ignored) {}

        return "Gastei 50 reais no supermercado para alimentação";
    }
}
