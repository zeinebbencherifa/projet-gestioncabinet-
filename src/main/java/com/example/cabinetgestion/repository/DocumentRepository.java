package com.example.cabinetgestion.repository;

import com.example.cabinetgestion.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByPatientId(Long patientId);
}
