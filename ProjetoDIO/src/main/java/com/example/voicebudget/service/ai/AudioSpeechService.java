package com.example.voicebudget.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class AudioSpeechService {

    private static final Logger log = LoggerFactory.getLogger(AudioSpeechService.class);

    private final OpenAiAudioSpeechModel speechModel;

    @Value("${spring.ai.openai.api-key:mock-key}")
    private String apiKey;

    public AudioSpeechService(@Autowired(required = false) OpenAiAudioSpeechModel speechModel) {
        this.speechModel = speechModel;
    }

    public byte[] sintetizarVozBytes(String texto) {
        if (texto == null || texto.isBlank()) {
            return new byte[0];
        }

        if (isMockMode()) {
            log.info("[TTS] Modo simulado ou chave não configurada. Gerando resposta de áudio sintética.");
            return gerarAudioSinteticoSimulado();
        }

        try {
            log.info("[TTS] Sintetizando voz com OpenAI TTS para o texto: '{}'", texto);
            OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                    .withModel("tts-1")
                    .withVoice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
                    .withResponseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                    .build();

            SpeechPrompt prompt = new SpeechPrompt(texto, options);
            SpeechResponse response = speechModel.call(prompt);
            return response.getResult().getOutput();
        } catch (Exception e) {
            log.warn("[TTS] Falha ao sintetizar voz via OpenAI TTS ({}). Usando fallback.", e.getMessage());
            return gerarAudioSinteticoSimulado();
        }
    }

    public String sintetizarVozBase64(String texto) {
        byte[] audio = sintetizarVozBytes(texto);
        if (audio != null && audio.length > 0) {
            return Base64.getEncoder().encodeToString(audio);
        }
        return null;
    }

    private boolean isMockMode() {
        return speechModel == null || apiKey == null || apiKey.isBlank() || apiKey.equalsIgnoreCase("mock-key");
    }

    private byte[] gerarAudioSinteticoSimulado() {
        // Gera um pequeno cabeçalho WAV audível para simulação de áudio quando offline
        // 44-byte WAV header padrão PCM 8000Hz mono
        int sampleRate = 8000;
        int durationMs = 300;
        int numSamples = (sampleRate * durationMs) / 1000;
        int dataSize = numSamples * 2;
        byte[] wav = new byte[44 + dataSize];

        // RIFF header
        wav[0] = 'R'; wav[1] = 'I'; wav[2] = 'F'; wav[3] = 'F';
        int fileSize = 36 + dataSize;
        wav[4] = (byte)(fileSize & 0xff);
        wav[5] = (byte)((fileSize >> 8) & 0xff);
        wav[6] = (byte)((fileSize >> 16) & 0xff);
        wav[7] = (byte)((fileSize >> 24) & 0xff);
        wav[8] = 'W'; wav[9] = 'A'; wav[10] = 'V'; wav[11] = 'E';

        // fmt chunk
        wav[12] = 'f'; wav[13] = 'm'; wav[14] = 't'; wav[15] = ' ';
        wav[16] = 16; wav[17] = 0; wav[18] = 0; wav[19] = 0; // chunk size
        wav[20] = 1; wav[21] = 0; // PCM format
        wav[22] = 1; wav[23] = 0; // Mono (1 channel)
        wav[24] = (byte)(sampleRate & 0xff);
        wav[25] = (byte)((sampleRate >> 8) & 0xff);
        wav[26] = 0; wav[27] = 0;
        int byteRate = sampleRate * 2;
        wav[28] = (byte)(byteRate & 0xff);
        wav[29] = (byte)((byteRate >> 8) & 0xff);
        wav[30] = 0; wav[31] = 0;
        wav[32] = 2; wav[33] = 0; // block align
        wav[34] = 16; wav[35] = 0; // bits per sample

        // data chunk
        wav[36] = 'd'; wav[37] = 'a'; wav[38] = 't'; wav[39] = 'a';
        wav[40] = (byte)(dataSize & 0xff);
        wav[41] = (byte)((dataSize >> 8) & 0xff);
        wav[42] = (byte)((dataSize >> 16) & 0xff);
        wav[43] = (byte)((dataSize >> 24) & 0xff);

        // Gera tom senoidal suave (beeps de confirmação financeira 440Hz / 880Hz)
        for (int i = 0; i < numSamples; i++) {
            double freq = i < numSamples / 2 ? 587.33 : 880.00; // Tons D5 e A5 agradáveis
            short sample = (short)(Math.sin(2 * Math.PI * freq * i / sampleRate) * 12000);
            wav[44 + i * 2] = (byte)(sample & 0xff);
            wav[44 + i * 2 + 1] = (byte)((sample >> 8) & 0xff);
        }

        return wav;
    }
}
