package com.example.cabinetgestion.service;

import com.example.cabinetgestion.entities.Document;
import com.example.cabinetgestion.entities.TypeDocument;
import com.example.cabinetgestion.entities.Utilisateur;
import com.example.cabinetgestion.repository.DocumentRepository;
import com.example.cabinetgestion.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService implements IserviceDocument {

    private final DocumentRepository documentRepository;
    private final UtilisateurRepository utilisateurRepository;

    private final String UPLOAD_DIR = "uploads/";

    @Override
    public Document uploadDocument(Long patientId, String typeDoc, String description, MultipartFile file) {

        Utilisateur patient = utilisateurRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient introuvable"));

        try {

            Files.createDirectories(Paths.get(UPLOAD_DIR));

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = UPLOAD_DIR + fileName;
            //sauvgarde dans le disque physique

            file.transferTo(new File(filePath));

            Document document = new Document();
            document.setPatient(patient);
            document.setDescription(description);
            document.setFileName(fileName);
            document.setFilePath(filePath);
            document.setFileType(file.getContentType());
            document.setTypeDocument(TypeDocument.valueOf(typeDoc.toUpperCase()));

            return documentRepository.save(document);

        } catch (IOException e) {
            throw new RuntimeException("Erreur upload fichier : " + e.getMessage());
        }
    }
    public void saveDocumentsForPatient(MultipartFile[] files,
                                        String type,
                                        String description,
                                        Long patientId) {

        Utilisateur patient = utilisateurRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient introuvable"));

        for (MultipartFile file : files) {
            try {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

                Path path = Paths.get("uploads/" + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                Document doc = new Document();
                doc.setFileName(fileName);
                doc.setTypeDocument(TypeDocument.valueOf(type.toUpperCase()));
                doc.setDescription(description);
                doc.setPatient(patient);
                doc.setUploadDate(LocalDateTime.now());
                doc.setFilePath(path.toString());

                documentRepository.save(doc);

            } catch (Exception e) {
                throw new RuntimeException("Erreur upload fichier : " + e.getMessage());
            }
        }
    }



    @Override
    public List<Document> getDocumentsByPatient(Long patientId) {
        return documentRepository.findByPatientId(patientId);
    }

    @Override
    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
    @Override
    public Document getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document introuvable"));
    }
}
