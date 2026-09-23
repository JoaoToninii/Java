package com.example.voicebudget.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MockFinancialIntentProcessor {

    private static final Logger log = LoggerFactory.getLogger(MockFinancialIntentProcessor.class);

    private final BudgetToolsService toolsService;

    public MockFinancialIntentProcessor(BudgetToolsService toolsService) {
        this.toolsService = toolsService;
    }

    public ProcessResult processar(String texto) {
        log.info("[MOCK-INTENT] Processando intenção financeira para o comando: '{}'", texto);
        String normalizado = texto != null ? texto.toLowerCase().trim() : "";
        List<String> acoes = new ArrayList<>();

        // 1. Consulta de Saldo
        if (normalizado.contains("saldo") || normalizado.contains("quanto tenho") || normalizado.contains("quanto sobrou")) {
            acoes.add("consultarSaldo");
            String resposta = toolsService.consultarSaldo();
            return new ProcessResult("CONSULTA_SALDO", acoes, "Aqui está a situação atual das suas finanças: " + resposta);
        }

        // 2. Consulta de Resumo por Categoria
        if (normalizado.contains("categoria") || normalizado.contains("por tipo") || normalizado.contains("onde gastei") || normalizado.contains("distribuição")) {
            acoes.add("consultarResumoPorCategoria");
            String resposta = toolsService.consultarResumoPorCategoria();
            return new ProcessResult("CONSULTA_CATEGORIAS", acoes, "Análise do seu orçamento: " + resposta);
        }

        // 3. Consulta de Extrato / Histórico
        if (normalizado.contains("extrato") || normalizado.contains("historico") || normalizado.contains("ultimas transacoes") || normalizado.contains("ultimos gastos")) {
            acoes.add("consultarExtrato");
            String resposta = toolsService.consultarExtrato("TODAS", 5);
            return new ProcessResult("CONSULTA_EXTRATO", acoes, resposta);
        }

        // 4. Definir Limite de Orçamento
        Pattern patLimite = Pattern.compile("(?:limite|teto|meta)\\s+(?:de\\s+)?(?:r\\$\\s*)?(\\d+(?:[,.]\\d+)?)\\s*(?:reais)?\\s*(?:para|em|na categoria)?\\s*([a-zà-ú]+)?");
        Matcher matLimite = patLimite.matcher(normalizado);
        if (matLimite.find()) {
            double valor = extrairValor(matLimite.group(1));
            String cat = matLimite.group(2) != null ? matLimite.group(2) : "ALIMENTACAO";
            acoes.add("definirLimiteOrcamento");
            String resposta = toolsService.definirLimiteOrcamento(cat, valor);
            return new ProcessResult("DEFINIR_ORCAMENTO", acoes, resposta);
        }

        // 5. Registro de Transação (Receita ou Despesa)
        Double valor = extrairValorDoTexto(normalizado);
        if (valor != null && valor > 0) {
            boolean isReceita = normalizado.contains("ganh") || normalizado.contains("receb") || normalizado.contains("salari") || normalizado.contains("deposito") || normalizado.contains("entrada");
            String tipo = isReceita ? "RECEITA" : "DESPESA";
            String categoria = inferirCategoria(normalizado, isReceita);
            String descricao = inferirDescricao(normalizado, categoria);

            acoes.add("registrarTransacao");
            String resposta = toolsService.registrarTransacao(tipo, valor, categoria, descricao);
            return new ProcessResult("REGISTRAR_TRANSACAO", acoes, resposta);
        }

        // Fallback genérico quando o comando não foi compreendido
        return new ProcessResult(
                "AJUDA_COMANDOS",
                acoes,
                "Não consegui identificar um valor ou operação financeira clara no seu comando. Você pode tentar frases como: " +
                        "'Gastei 50 reais no almoço', 'Recebi 3500 de salário', 'Qual o meu saldo?' ou 'Definir limite de 600 para alimentação'."
        );
    }

    private Double extrairValorDoTexto(String texto) {
        Pattern p = Pattern.compile("(?:r\\$\\s*)?(\\d+(?:[,.]\\d+)?)(?:\\s*reais)?");
        Matcher m = p.matcher(texto);
        if (m.find()) {
            return extrairValor(m.group(1));
        }
        return null;
    }

    private double extrairValor(String str) {
        try {
            return Double.parseDouble(str.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String inferirCategoria(String texto, boolean isReceita) {
        if (isReceita) {
            return "SALARIO";
        }
        if (texto.contains("almo") || texto.contains("jant") || texto.contains("comida") || texto.contains("mercado") || texto.contains("lanche") || texto.contains("padaria") || texto.contains("pizza")) {
            return "ALIMENTACAO";
        }
        if (texto.contains("uber") || texto.contains("gasolina") || texto.contains("combust") || texto.contains("onibus") || texto.contains("passag") || texto.contains("carro")) {
            return "TRANSPORTE";
        }
        if (texto.contains("aluguel") || texto.contains("condominio") || texto.contains("luz") || texto.contains("agua") || texto.contains("internet") || texto.contains("casa")) {
            return "MORADIA";
        }
        if (texto.contains("cinema") || texto.contains("festa") || texto.contains("bar") || texto.contains("jogo") || texto.contains("cerveja") || texto.contains("viag")) {
            return "LAZER";
        }
        if (texto.contains("farmacia") || texto.contains("remedio") || texto.contains("medico") || texto.contains("hospital") || texto.contains("consulta")) {
            return "SAUDE";
        }
        if (texto.contains("curso") || texto.contains("livro") || texto.contains("faculdade") || texto.contains("escola")) {
            return "EDUCACAO";
        }
        return "OUTROS";
    }

    private String inferirDescricao(String texto, String categoria) {
        String desc = texto.replaceAll("(?:gastei|paguei|comprei|recebi|depositei|r\\$\\s*|\\d+(?:[,.]\\d+)?|reais|no|na|de|com|para|hoje|ontem)", " ").trim();
        if (desc.isBlank()) {
            return "Transação em " + categoria;
        }
        return desc.substring(0, 1).toUpperCase() + desc.substring(1);
    }

    public record ProcessResult(
            String intencao,
            List<String> acoesExecutadas,
            String resposta
    ) {}
}
