package com.example.voicebudget;

import com.example.voicebudget.dto.SaldoDTO;
import com.example.voicebudget.model.CategoriaTransacao;
import com.example.voicebudget.model.TipoTransacao;
import com.example.voicebudget.service.OrcamentoService;
import com.example.voicebudget.service.TransacaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TransacaoServiceTest {

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private OrcamentoService orcamentoService;

    @Test
    @DisplayName("Deve registrar receita e despesa e calcular o saldo corretamente")
    void deveRegistrarTransacaoECalcularSaldo() {
        // Cenário: registrar uma receita de 1000 e despesa de 300
        transacaoService.registrarTransacao(
                TipoTransacao.RECEITA,
                BigDecimal.valueOf(1000.00),
                CategoriaTransacao.SALARIO,
                "Bônus de projeto",
                LocalDate.now()
        );

        transacaoService.registrarTransacao(
                TipoTransacao.DESPESA,
                BigDecimal.valueOf(300.00),
                CategoriaTransacao.LAZER,
                "Show de música",
                LocalDate.now()
        );

        SaldoDTO saldo = transacaoService.obterSaldo();
        assertNotNull(saldo);
        assertTrue(saldo.saldoAtual().compareTo(BigDecimal.ZERO) > 0);
        assertEquals("POSITIVO", saldo.status());
    }

    @Test
    @DisplayName("Deve emitir alerta quando despesa ultrapassar o limite do orçamento")
    void deveEmitirAlertaQuandoUltrapassarLimite() {
        // Define limite de 100 para transporte
        orcamentoService.definirLimite(CategoriaTransacao.TRANSPORTE, BigDecimal.valueOf(100.00));

        // Registra despesa de 150 em transporte
        TransacaoService.TransacaoRegistroResultado resultado = transacaoService.registrarTransacao(
                TipoTransacao.DESPESA,
                BigDecimal.valueOf(150.00),
                CategoriaTransacao.TRANSPORTE,
                "Viagem intermunicipal",
                LocalDate.now()
        );

        assertNotNull(resultado.alertaOrcamento());
        assertTrue(resultado.alertaOrcamento().contains("ATENÇÃO: Você ultrapassou o orçamento"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar transação com valor inválido")
    void deveFalharComValorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> {
            transacaoService.registrarTransacao(
                    TipoTransacao.DESPESA,
                    BigDecimal.valueOf(-50.00),
                    CategoriaTransacao.ALIMENTACAO,
                    "Teste inválido",
                    LocalDate.now()
            );
        });
    }
}
