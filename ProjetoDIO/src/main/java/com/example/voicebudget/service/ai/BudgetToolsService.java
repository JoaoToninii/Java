package com.example.voicebudget.service.ai;

import com.example.voicebudget.dto.ResumoCategoriaDTO;
import com.example.voicebudget.dto.SaldoDTO;
import com.example.voicebudget.dto.TransacaoDTO;
import com.example.voicebudget.model.CategoriaTransacao;
import com.example.voicebudget.model.TipoTransacao;
import com.example.voicebudget.service.OrcamentoService;
import com.example.voicebudget.service.TransacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BudgetToolsService {

    private static final Logger log = LoggerFactory.getLogger(BudgetToolsService.class);

    private final TransacaoService transacaoService;
    private final OrcamentoService orcamentoService;

    public BudgetToolsService(TransacaoService transacaoService, OrcamentoService orcamentoService) {
        this.transacaoService = transacaoService;
        this.orcamentoService = orcamentoService;
    }

    // Records para Function Calling
    public record RegistrarTransacaoRequest(String tipo, Double valor, String categoria, String descricao) {}
    public record ToolResponse(String resultado) {}
    public record ConsultarSaldoRequest(String filtro) {}
    public record ConsultarExtratoRequest(String categoria, Integer limite) {}
    public record ConsultarResumoRequest(String periodo) {}
    public record DefinirLimiteRequest(String categoria, Double valorLimite) {}

    public ToolResponse registrarTransacaoFunction(RegistrarTransacaoRequest req) {
        String msg = registrarTransacao(req.tipo(), req.valor(), req.categoria(), req.descricao());
        return new ToolResponse(msg);
    }

    public ToolResponse consultarSaldoFunction(ConsultarSaldoRequest req) {
        return new ToolResponse(consultarSaldo());
    }

    public ToolResponse consultarExtratoFunction(ConsultarExtratoRequest req) {
        return new ToolResponse(consultarExtrato(req.categoria(), req.limite()));
    }

    public ToolResponse consultarResumoFunction(ConsultarResumoRequest req) {
        return new ToolResponse(consultarResumoPorCategoria());
    }

    public ToolResponse definirLimiteFunction(DefinirLimiteRequest req) {
        return new ToolResponse(definirLimiteOrcamento(req.categoria(), req.valorLimite()));
    }

    public String registrarTransacao(String tipo, Double valor, String categoria, String descricao) {
        log.info("[TOOL] Executando registrarTransacao: tipo={}, valor={}, categoria={}, desc={}", tipo, valor, categoria, descricao);
        try {
            TipoTransacao tipoEnum = TipoTransacao.fromString(tipo);
            CategoriaTransacao catEnum = CategoriaTransacao.fromString(categoria);
            BigDecimal valorBd = BigDecimal.valueOf(valor != null ? valor : 0.0);

            TransacaoService.TransacaoRegistroResultado resultado = transacaoService.registrarTransacao(
                    tipoEnum, valorBd, catEnum, descricao, LocalDate.now()
            );

            StringBuilder resposta = new StringBuilder();
            resposta.append(String.format("Sucesso! %s de R$ %.2f registrada em %s (%s). ",
                    tipoEnum == TipoTransacao.RECEITA ? "Receita" : "Despesa",
                    valor, catEnum.name(), descricao));

            if (resultado.alertaOrcamento() != null) {
                resposta.append(resultado.alertaOrcamento()).append(" ");
            }

            SaldoDTO saldo = resultado.saldoAtualizado();
            resposta.append(String.format("Saldo atual consolidado: R$ %.2f.", saldo.saldoAtual()));

            return resposta.toString();
        } catch (Exception e) {
            log.error("Erro ao registrar transação via tool: {}", e.getMessage());
            return "Erro ao registrar transação: " + e.getMessage();
        }
    }

    public String consultarSaldo() {
        log.info("[TOOL] Executando consultarSaldo");
        try {
            SaldoDTO saldo = transacaoService.obterSaldo();
            return String.format("Saldo Atual: R$ %.2f (Receitas: R$ %.2f | Despesas: R$ %.2f | Status: %s).",
                    saldo.saldoAtual(), saldo.totalReceitas(), saldo.totalDespesas(), saldo.status());
        } catch (Exception e) {
            log.error("Erro ao consultar saldo via tool: {}", e.getMessage());
            return "Erro ao consultar saldo: " + e.getMessage();
        }
    }

    public String consultarExtrato(String categoria, Integer limite) {
        log.info("[TOOL] Executando consultarExtrato: categoria={}, limite={}", categoria, limite);
        try {
            int max = (limite != null && limite > 0) ? limite : 5;
            List<TransacaoDTO> lista;

            if (categoria != null && !categoria.isBlank() && !categoria.equalsIgnoreCase("TODAS")) {
                CategoriaTransacao catEnum = CategoriaTransacao.fromString(categoria);
                lista = transacaoService.listarPorCategoria(catEnum);
            } else {
                lista = transacaoService.listarRecentes(max);
            }

            if (lista.isEmpty()) {
                return "Nenhuma transação encontrada para os critérios informados.";
            }

            StringBuilder sb = new StringBuilder("Extrato recente:\n");
            for (TransacaoDTO t : lista) {
                sb.append(String.format("- [%s] %s: R$ %.2f (%s - %s)\n",
                        t.dataTransacao(),
                        t.tipo(),
                        t.valor(),
                        t.categoria(),
                        t.descricao()));
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("Erro ao consultar extrato via tool: {}", e.getMessage());
            return "Erro ao consultar extrato: " + e.getMessage();
        }
    }

    public String consultarResumoPorCategoria() {
        log.info("[TOOL] Executando consultarResumoPorCategoria");
        try {
            List<ResumoCategoriaDTO> resumos = transacaoService.obterResumoCategorias();
            if (resumos.isEmpty()) {
                return "Ainda não existem despesas registradas para gerar resumo por categoria.";
            }

            StringBuilder sb = new StringBuilder("Resumo de Gastos por Categoria:\n");
            for (ResumoCategoriaDTO r : resumos) {
                sb.append(String.format("• %s: R$ %.2f", r.categoria(), r.totalGasto()));
                if (r.limiteMensal() != null) {
                    sb.append(String.format(" (Limite: R$ %.2f - %.1f%%)", r.limiteMensal(), r.porcentagemUtilizada()));
                }
                if (r.alerta() != null) {
                    sb.append(" -> ").append(r.alerta());
                }
                sb.append("\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("Erro ao consultar resumo de categorias: {}", e.getMessage());
            return "Erro ao consultar resumo de categorias: " + e.getMessage();
        }
    }

    public String definirLimiteOrcamento(String categoria, Double valorLimite) {
        log.info("[TOOL] Executando definirLimiteOrcamento: categoria={}, valor={}", categoria, valorLimite);
        try {
            CategoriaTransacao catEnum = CategoriaTransacao.fromString(categoria);
            orcamentoService.definirLimite(catEnum, BigDecimal.valueOf(valorLimite));
            return String.format("Limite de orçamento para a categoria %s foi definido com sucesso para R$ %.2f mensais.",
                    catEnum.name(), valorLimite);
        } catch (Exception e) {
            log.error("Erro ao definir limite via tool: {}", e.getMessage());
            return "Erro ao definir limite: " + e.getMessage();
        }
    }
}
