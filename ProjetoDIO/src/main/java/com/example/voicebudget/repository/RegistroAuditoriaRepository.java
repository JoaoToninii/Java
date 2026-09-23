package com.example.voicebudget.repository;

import com.example.voicebudget.model.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {
    List<RegistroAuditoria> findAllByOrderByDataHoraDesc();
    List<RegistroAuditoria> findTop20ByOrderByDataHoraDesc();
}
