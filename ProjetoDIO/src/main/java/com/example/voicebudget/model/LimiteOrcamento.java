package com.example.voicebudget.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "limites_orcamento")
public class LimiteOrcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private CategoriaTransacao categoria;

    @NotNull
    @DecimalMin(value = "0.01", message = "O limite deve ser maior que zero")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal limiteMensal;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public LimiteOrcamento() {
    }

    public LimiteOrcamento(CategoriaTransacao categoria, BigDecimal limiteMensal) {
        this.categoria = categoria;
        this.limiteMensal = limiteMensal;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CategoriaTransacao getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaTransacao categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getLimiteMensal() {
        return limiteMensal;
    }

    public void setLimiteMensal(BigDecimal limiteMensal) {
        this.limiteMensal = limiteMensal;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
