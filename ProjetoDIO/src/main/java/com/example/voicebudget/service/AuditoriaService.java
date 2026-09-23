package com.example.voicebudget.service;

import com.example.voicebudget.model.RegistroAuditoria;
import com.example.voicebudget.repository.RegistroAuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final RegistroAuditoriaRepository auditoriaRepository;

    public AuditoriaService(RegistroAuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional
    public RegistroAuditoria registrar(String canal,
                                       String entradaUsuario,
                                       String intencaoIdentificada,
                                       String ferramentasChamadas,
                                       String respostaGerada,
                                       Long tempoProcessamentoMs,
                                       boolean sucesso) {
        try {
            RegistroAuditoria registro = new RegistroAuditoria(
                    canal != null ? canal : "TEXTO",
                    entradaUsuario != null ? entradaUsuario : "",
                    intencaoIdentificada,
                    ferramentasChamadas,
                    respostaGerada,
                    tempoProcessamentoMs != null ? tempoProcessamentoMs : 0L,
                    sucesso
            );
            RegistroAuditoria salvo = auditoriaRepository.save(registro);
            log.info("[AUDITORIA] Canal: {} | Sucesso: {} | Tempo: {}ms | Ações: {}",
                    canal, sucesso, tempoProcessamentoMs, ferramentasChamadas);
            return salvo;
        } catch (Exception e) {
            log.error("Erro ao gravar registro de auditoria: {}", e.getMessage());
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<RegistroAuditoria> listarRecentes() {
        return auditoriaRepository.findTop20ByOrderByDataHoraDesc();
    }

    @Transactional(readOnly = true)
    public List<RegistroAuditoria> listarTodas() {
        return auditoriaRepository.findAllByOrderByDataHoraDesc();
    }
}
