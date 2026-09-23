package com.example.voicebudget.controller;

import com.example.voicebudget.dto.OrcamentoDTO;
import com.example.voicebudget.model.LimiteOrcamento;
import com.example.voicebudget.service.OrcamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orcamentos")
@CrossOrigin(origins = "*")
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    public OrcamentoController(OrcamentoService orcamentoService) {
        this.orcamentoService = orcamentoService;
    }

    @GetMapping
    public ResponseEntity<List<OrcamentoDTO>> listarTodos() {
        return ResponseEntity.ok(orcamentoService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<OrcamentoDTO> definirLimite(@Valid @RequestBody OrcamentoDTO dto) {
        LimiteOrcamento salvo = orcamentoService.definirLimite(dto.categoria(), dto.limiteMensal());
        return ResponseEntity.ok(OrcamentoDTO.fromEntity(salvo));
    }
}
