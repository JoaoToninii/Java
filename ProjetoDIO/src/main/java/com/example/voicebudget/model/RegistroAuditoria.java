package com.example.voicebudget.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_auditoria")
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String canal; // AUDIO, TEXTO, REST

    @Column(nullable = false, length = 1000)
    private String entradaUsuario;

    @Column(length = 255)
    private String intencaoIdentificada;

    @Column(length = 255)
    private String ferramentasChamadas;

    @Column(columnDefinition = "TEXT")
    private String respostaGerada;

    @Column(name = "tempo_processamento_ms")
    private Long tempoProcessamentoMs;

    @Column(nullable = false)
    private boolean sucesso;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    public RegistroAuditoria() {
    }

    public RegistroAuditoria(String canal, String entradaUsuario, String intencaoIdentificada, String ferramentasChamadas, String respostaGerada, Long tempoProcessamentoMs, boolean sucesso) {
        this.canal = canal;
        this.entradaUsuario = entradaUsuario;
        this.intencaoIdentificada = intencaoIdentificada;
        this.ferramentasChamadas = ferramentasChamadas;
        this.respostaGerada = respostaGerada;
        this.tempoProcessamentoMs = tempoProcessamentoMs;
        this.sucesso = sucesso;
        this.dataHora = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getEntradaUsuario() {
        return entradaUsuario;
    }

    public void setEntradaUsuario(String entradaUsuario) {
        this.entradaUsuario = entradaUsuario;
    }

    public String getIntencaoIdentificada() {
        return intencaoIdentificada;
    }

    public void setIntencaoIdentificada(String intencaoIdentificada) {
        this.intencaoIdentificada = intencaoIdentificada;
    }

    public String getFerramentasChamadas() {
        return ferramentasChamadas;
    }

    public void setFerramentasChamadas(String ferramentasChamadas) {
        this.ferramentasChamadas = ferramentasChamadas;
    }

    public String getRespostaGerada() {
        return respostaGerada;
    }

    public void setRespostaGerada(String respostaGerada) {
        this.respostaGerada = respostaGerada;
    }

    public Long getTempoProcessamentoMs() {
        return tempoProcessamentoMs;
    }

    public void setTempoProcessamentoMs(Long tempoProcessamentoMs) {
        this.tempoProcessamentoMs = tempoProcessamentoMs;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}
