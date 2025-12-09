package com.example.cabinetgestion.controlleur;

import com.example.cabinetgestion.entities.Role;
import com.example.cabinetgestion.entities.Utilisateur;
import com.example.cabinetgestion.service.ServiceUtilisateur;
import com.example.cabinetgestion.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/medecin/patient")
public class MedecinPatientController {

    private final ServiceUtilisateur serviceUtilisateur;
    private final DocumentService documentService;

    @GetMapping("/ajouter")
    public String formAjout(@RequestParam(required = false) String returnTo,
                            Model model,
                            Principal principal) {

        // ✅ Récupérer le médecin connecté
        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        model.addAttribute("patient", new Utilisateur());
        model.addAttribute("returnTo", returnTo);
        model.addAttribute("medecinId", medecin.getId()); // ✅ Passer l'ID du médecin

        return "medecin/ajouter-patient";
    }
    // Soumission du formulaire
    @PostMapping("/save")
    public String savePatient(@ModelAttribute Utilisateur patient,
                              @RequestParam("files") MultipartFile[] files,
                              @RequestParam("typeDocument") String typeDocument,
                              @RequestParam("description") String description) {

        patient.setRole(Role.PATIENT);
        Utilisateur saved = serviceUtilisateur.saveUtilisateur(patient);

        // Upload documents (optionnel)
        if (files != null && files.length > 0 && !files[0].isEmpty()) {
            documentService.saveDocumentsForPatient(files, typeDocument, description, saved.getId());
        }

        return "redirect:/medecin/home";
    }
}
