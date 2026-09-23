package com.example.voicebudget.service.ai;

import com.example.voicebudget.dto.SaldoDTO;
import com.example.voicebudget.dto.VoiceProcessResponseDTO;
import com.example.voicebudget.service.AuditoriaService;
import com.example.voicebudget.service.TransacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class BudgetAssistantService {

    private static final Logger log = LoggerFactory.getLogger(BudgetAssistantService.class);

    private static final String SYSTEM_PROMPT = """
            Você é o assistente virtual financeiro e de orçamento inteligente do usuário.
            Seu objetivo é gerenciar as finanças pessoais com precisão, educação e clareza.
            
            Suas capacidades incluem:
            1. Registrar receitas (salários, depósitos, rendimentos) e despesas (alimentação, transporte, lazer, moradia, etc).
            2. Consultar o saldo consolidado atual.
            3. Consultar extratos de transações recentes.
            4. Consultar resumo de gastos por categoria e alertar sobre limites.
            5. Definir ou ajustar limites orçamentários por categoria.
            
            DIRETRIZES:
            - Sempre utilize as funções (tools) disponíveis para executar ações reais e obter dados do banco.
            - Responda em português brasileiro de forma natural, amigável e concisa, ideal para ser ouvida por voz.
            - Sempre confirme os valores e categorias das operações realizadas.
            - Se o usuário ultrapassar ou se aproximar do limite de uma categoria, destaque o alerta.
            """;

    private final AudioTranscriptionService transcriptionService;
    private final AudioSpeechService speechService;
    private final BudgetToolsService budgetToolsService;
    private final MockFinancialIntentProcessor mockProcessor;
    private final TransacaoService transacaoService;
    private final AuditoriaService auditoriaService;
    private final ChatClient chatClient;

    @Value("${spring.ai.openai.api-key:mock-key}")
    private String apiKey;

    public BudgetAssistantService(AudioTranscriptionService transcriptionService,
                                  AudioSpeechService speechService,
                                  BudgetToolsService budgetToolsService,
                                  MockFinancialIntentProcessor mockProcessor,
                                  TransacaoService transacaoService,
                                  AuditoriaService auditoriaService,
                                  @Autowired(required = false) ChatModel chatModel) {
        this.transcriptionService = transcriptionService;
        this.speechService = speechService;
        this.budgetToolsService = budgetToolsService;
        this.mockProcessor = mockProcessor;
        this.transacaoService = transacaoService;
        this.auditoriaService = auditoriaService;

        if (chatModel != null) {
            this.chatClient = ChatClient.builder(chatModel)
                    .defaultSystem(SYSTEM_PROMPT)
                    .defaultFunction("registrarTransacao",
                            "Registra uma nova transação financeira de receita ou despesa.",
                            budgetToolsService::registrarTransacaoFunction)
                    .defaultFunction("consultarSaldo",
                            "Consulta o saldo financeiro atual do usuário, receitas e despesas.",
                            budgetToolsService::consultarSaldoFunction)
                    .defaultFunction("consultarExtrato",
                            "Consulta o histórico de transações recentes por categoria ou geral.",
                            budgetToolsService::consultarExtratoFunction)
                    .defaultFunction("consultarResumoPorCategoria",
                            "Consulta o resumo de despesas acumuladas agrupadas por categoria e alertas.",
                            budgetToolsService::consultarResumoFunction)
                    .defaultFunction("definirLimiteOrcamento",
                            "Define ou altera o teto de orçamento mensal para uma categoria.",
                            budgetToolsService::definirLimiteFunction)
                    .build();
        } else {
            this.chatClient = null;
        }
    }

    public VoiceProcessResponseDTO processarAudio(MultipartFile audioFile) {
        long inicio = System.currentTimeMillis();
        String transcricao = "";
        try {
            transcricao = transcriptionService.transcreverAudio(audioFile);
            return executarFluxoIA("AUDIO", transcricao, inicio);
        } catch (Exception e) {
            long duracao = System.currentTimeMillis() - inicio;
            log.error("Erro no processamento de áudio: {}", e.getMessage(), e);
            auditoriaService.registrar("AUDIO", transcricao, "ERRO", "none", e.getMessage(), duracao, false);
            return new VoiceProcessResponseDTO(
                    "AUDIO",
                    transcricao,
                    "Desculpe, ocorreu um erro ao processar o seu comando de áudio: " + e.getMessage(),
                    null,
                    List.of(),
                    transacaoService.obterSaldo(),
                    duracao,
                    false
            );
        }
    }

    public VoiceProcessResponseDTO processarTexto(String comandoTexto) {
        long inicio = System.currentTimeMillis();
        return executarFluxoIA("TEXTO", comandoTexto, inicio);
    }

    private VoiceProcessResponseDTO executarFluxoIA(String canal, String textoComando, long inicioMs) {
        String respostaFinal;
        List<String> acoesExecutadas = new ArrayList<>();
        String intencao = "PROCESSAR_COMANDO";
        boolean sucesso = true;

        if (isMockMode()) {
            log.info("[ASSISTANT] Operando em modo de processamento inteligente local / sem chave OpenAI.");
            MockFinancialIntentProcessor.ProcessResult resultado = mockProcessor.processar(textoComando);
            respostaFinal = resultado.resposta();
            intencao = resultado.intencao();
            acoesExecutadas.addAll(resultado.acoesExecutadas());
        } else {
            try {
                log.info("[ASSISTANT] Chamando ChatClient do Spring AI com Functions para: '{}'", textoComando);
                respostaFinal = chatClient.prompt()
                        .user(textoComando)
                        .call()
                        .content();
                acoesExecutadas.add("springAiChatClientWithFunctions");
            } catch (Exception e) {
                log.warn("[ASSISTANT] Falha na chamada da OpenAI ({}). Acionando fallback inteligente.", e.getMessage());
                MockFinancialIntentProcessor.ProcessResult resultado = mockProcessor.processar(textoComando);
                respostaFinal = resultado.resposta();
                intencao = resultado.intencao();
                acoesExecutadas.addAll(resultado.acoesExecutadas());
            }
        }

        // Sintetizar voz para retorno
        String audioBase64 = null;
        try {
            audioBase64 = speechService.sintetizarVozBase64(respostaFinal);
        } catch (Exception e) {
            log.warn("[ASSISTANT] Não foi possível sintetizar áudio de resposta: {}", e.getMessage());
        }

        SaldoDTO saldoAtual = transacaoService.obterSaldo();
        long duracaoMs = System.currentTimeMillis() - inicioMs;

        // Registrar auditoria
        auditoriaService.registrar(
                canal,
                textoComando,
                intencao,
                String.join(", ", acoesExecutadas),
                respostaFinal,
                duracaoMs,
                sucesso
        );

        return new VoiceProcessResponseDTO(
                canal,
                textoComando,
                respostaFinal,
                audioBase64,
                acoesExecutadas,
                saldoAtual,
                duracaoMs,
                sucesso
        );
    }

    private boolean isMockMode() {
        return chatClient == null || apiKey == null || apiKey.isBlank() || apiKey.equalsIgnoreCase("mock-key");
    }
}
