package com.example.cabinetgestion.service;



import com.example.cabinetgestion.entities.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IserviceDocument{

    Document uploadDocument(Long patientId,
                            String typeDocument,
                            String description,
                            MultipartFile file);

    List<Document> getDocumentsByPatient(Long patientId);

    void deleteDocument(Long id);

    Document getDocumentById(Long documentId);
}
