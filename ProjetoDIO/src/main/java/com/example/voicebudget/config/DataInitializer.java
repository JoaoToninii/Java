package com.example.voicebudget.config;

import com.example.voicebudget.model.CategoriaTransacao;
import com.example.voicebudget.model.TipoTransacao;
import com.example.voicebudget.service.OrcamentoService;
import com.example.voicebudget.service.TransacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(TransacaoService transacaoService, OrcamentoService orcamentoService) {
        return args -> {
            log.info("[INIT] Inicializando dados de exemplo para demonstração...");

            // Limites de orçamento
            orcamentoService.definirLimite(CategoriaTransacao.ALIMENTACAO, BigDecimal.valueOf(1200.00));
            orcamentoService.definirLimite(CategoriaTransacao.TRANSPORTE, BigDecimal.valueOf(400.00));
            orcamentoService.definirLimite(CategoriaTransacao.LAZER, BigDecimal.valueOf(500.00));

            // Transações de exemplo
            transacaoService.registrarTransacao(
                    TipoTransacao.RECEITA,
                    BigDecimal.valueOf(5500.00),
                    CategoriaTransacao.SALARIO,
                    "Salário mensal",
                    LocalDate.now().minusDays(5)
            );

            transacaoService.registrarTransacao(
                    TipoTransacao.DESPESA,
                    BigDecimal.valueOf(250.00),
                    CategoriaTransacao.ALIMENTACAO,
                    "Supermercado semanal",
                    LocalDate.now().minusDays(3)
            );

            transacaoService.registrarTransacao(
                    TipoTransacao.DESPESA,
                    BigDecimal.valueOf(45.50),
                    CategoriaTransacao.TRANSPORTE,
                    "Corrida de aplicativo",
                    LocalDate.now().minusDays(1)
            );

            log.info("[INIT] Dados de exemplo inicializados com sucesso! Saldo inicial: R$ {}",
                    transacaoService.obterSaldo().saldoAtual());
        };
    }
}
