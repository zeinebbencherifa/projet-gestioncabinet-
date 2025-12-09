package com.example.cabinetgestion.controlleur;

import com.example.cabinetgestion.entities.Document;
import com.example.cabinetgestion.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Upload documents (depuis ton modal)
     */
    @PostMapping("/upload")
    public String uploadDocument(@RequestParam("patientId") Long patientId,
                                 @RequestParam("typeDocument") String typeDocument,
                                 @RequestParam("description") String description,
                                 @RequestParam("files") MultipartFile file,
                                 Model model) {

        try {
            documentService.uploadDocument(patientId, typeDocument, description, file);
            model.addAttribute("success", "Document ajouté avec succès !");
        } catch (Exception e) {
            model.addAttribute("error", "Erreur : " + e.getMessage());
        }

        // Redirection vers la page patient après upload
        return "redirect:/patients/details/" + patientId;
    }

    /**
     * Liste des documents d’un patient
     */
    @GetMapping("/patient/{id}")
    public String getDocumentsByPatient(@PathVariable Long id, Model model) {
        List<Document> documents = documentService.getDocumentsByPatient(id);

        model.addAttribute("documents", documents);
        model.addAttribute("patientId", id);

        return "documents/liste"; // tu peux changer selon ta vue
    }

    /**
     * Supprimer un document
     */
    @GetMapping("/delete/{id}")
    public String deleteDocument(@PathVariable Long id,
                                 @RequestParam("patientId") Long patientId) {

        documentService.deleteDocument(id);

        return "redirect:/patients/details/" + patientId;
    }
}
