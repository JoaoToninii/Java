package com.example.voicebudget.controller;

import com.example.voicebudget.dto.ResumoCategoriaDTO;
import com.example.voicebudget.dto.SaldoDTO;
import com.example.voicebudget.dto.TransacaoDTO;
import com.example.voicebudget.model.CategoriaTransacao;
import com.example.voicebudget.model.TipoTransacao;
import com.example.voicebudget.service.TransacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transacoes")
@CrossOrigin(origins = "*")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public ResponseEntity<List<TransacaoDTO>> listar(
            @RequestParam(required = false) CategoriaTransacao categoria,
            @RequestParam(required = false) TipoTransacao tipo,
            @RequestParam(defaultValue = "20") int limit) {
        if (categoria != null) {
            return ResponseEntity.ok(transacaoService.listarPorCategoria(categoria));
        }
        if (tipo != null) {
            return ResponseEntity.ok(transacaoService.listarPorTipo(tipo));
        }
        return ResponseEntity.ok(transacaoService.listarRecentes(limit));
    }

    @GetMapping("/saldo")
    public ResponseEntity<SaldoDTO> obterSaldo() {
        return ResponseEntity.ok(transacaoService.obterSaldo());
    }

    @GetMapping("/resumo-categorias")
    public ResponseEntity<List<ResumoCategoriaDTO>> obterResumoCategorias() {
        return ResponseEntity.ok(transacaoService.obterResumoCategorias());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        boolean removido = transacaoService.excluir(id);
        return removido ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
