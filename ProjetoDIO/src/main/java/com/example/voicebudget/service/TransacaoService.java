package com.example.voicebudget.service;

import com.example.voicebudget.dto.ResumoCategoriaDTO;
import com.example.voicebudget.dto.SaldoDTO;
import com.example.voicebudget.dto.TransacaoDTO;
import com.example.voicebudget.model.CategoriaTransacao;
import com.example.voicebudget.model.LimiteOrcamento;
import com.example.voicebudget.model.TipoTransacao;
import com.example.voicebudget.model.Transacao;
import com.example.voicebudget.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final OrcamentoService orcamentoService;

    public TransacaoService(TransacaoRepository transacaoRepository, OrcamentoService orcamentoService) {
        this.transacaoRepository = transacaoRepository;
        this.orcamentoService = orcamentoService;
    }

    @Transactional
    public TransacaoRegistroResultado registrarTransacao(TipoTransacao tipo,
                                                        BigDecimal valor,
                                                        CategoriaTransacao categoria,
                                                        String descricao,
                                                        LocalDate dataTransacao) {
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo da transação (RECEITA ou DESPESA) é obrigatório.");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transação deve ser positivo e maior que zero.");
        }
        if (categoria == null) {
            categoria = CategoriaTransacao.OUTROS;
        }
        if (descricao == null || descricao.isBlank()) {
            descricao = tipo == TipoTransacao.RECEITA ? "Receita registrada" : "Despesa registrada";
        }
        if (dataTransacao == null) {
            dataTransacao = LocalDate.now();
        }

        Transacao transacao = new Transacao(tipo, valor, categoria, descricao, dataTransacao);
        Transacao salva = transacaoRepository.save(transacao);

        String alertaOrcamento = null;
        if (tipo == TipoTransacao.DESPESA) {
            BigDecimal totalGastoCategoria = transacaoRepository.sumDespesasByCategoria(categoria);
            Optional<LimiteOrcamento> limiteOpt = orcamentoService.obterLimite(categoria);
            if (limiteOpt.isPresent()) {
                alertaOrcamento = orcamentoService.verificarAlerta(categoria, totalGastoCategoria, limiteOpt.get().getLimiteMensal());
            }
        }

        SaldoDTO saldoAtual = obterSaldo();
        return new TransacaoRegistroResultado(TransacaoDTO.fromEntity(salva), saldoAtual, alertaOrcamento);
    }

    @Transactional(readOnly = true)
    public SaldoDTO obterSaldo() {
        BigDecimal receitas = transacaoRepository.sumValorByTipo(TipoTransacao.RECEITA);
        BigDecimal despesas = transacaoRepository.sumValorByTipo(TipoTransacao.DESPESA);
        return SaldoDTO.of(receitas, despesas);
    }

    @Transactional(readOnly = true)
    public List<TransacaoDTO> listarTodas() {
        return transacaoRepository.findAllByOrderByDataTransacaoDescCreatedAtDesc().stream()
                .map(TransacaoDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransacaoDTO> listarRecentes(int limit) {
        return transacaoRepository.findTop10ByOrderByDataTransacaoDescCreatedAtDesc().stream()
                .limit(limit > 0 ? limit : 10)
                .map(TransacaoDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransacaoDTO> listarPorCategoria(CategoriaTransacao categoria) {
        return transacaoRepository.findByCategoriaOrderByDataTransacaoDesc(categoria).stream()
                .map(TransacaoDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransacaoDTO> listarPorTipo(TipoTransacao tipo) {
        return transacaoRepository.findByTipoOrderByDataTransacaoDesc(tipo).stream()
                .map(TransacaoDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResumoCategoriaDTO> obterResumoCategorias() {
        List<Object[]> agrupados = transacaoRepository.findTotalDespesasAgrupadasPorCategoria();
        Map<CategoriaTransacao, BigDecimal> mapaGastos = new EnumMap<>(CategoriaTransacao.class);

        for (Object[] row : agrupados) {
            CategoriaTransacao cat = (CategoriaTransacao) row[0];
            BigDecimal total = (BigDecimal) row[1];
            mapaGastos.put(cat, total);
        }

        List<ResumoCategoriaDTO> resumos = new ArrayList<>();
        for (CategoriaTransacao cat : CategoriaTransacao.values()) {
            BigDecimal gasto = mapaGastos.getOrDefault(cat, BigDecimal.ZERO);
            if (gasto.compareTo(BigDecimal.ZERO) > 0 || orcamentoService.obterLimite(cat).isPresent()) {
                Optional<LimiteOrcamento> limiteOpt = orcamentoService.obterLimite(cat);
                BigDecimal limite = limiteOpt.map(LimiteOrcamento::getLimiteMensal).orElse(null);
                Double percentual = null;
                String alerta = null;

                if (limite != null && limite.compareTo(BigDecimal.ZERO) > 0) {
                    percentual = gasto.divide(limite, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
                    alerta = orcamentoService.verificarAlerta(cat, gasto, limite);
                }

                resumos.add(new ResumoCategoriaDTO(cat, gasto, limite, percentual, alerta));
            }
        }
        return resumos;
    }

    @Transactional
    public boolean excluir(Long id) {
        if (transacaoRepository.existsById(id)) {
            transacaoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public record TransacaoRegistroResultado(
            TransacaoDTO transacao,
            SaldoDTO saldoAtualizado,
            String alertaOrcamento
    ) {}
}
