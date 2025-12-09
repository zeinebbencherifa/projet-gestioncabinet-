package com.example.cabinetgestion.controlleur;

import com.example.cabinetgestion.entities.Document;
import com.example.cabinetgestion.entities.Rdv;
import com.example.cabinetgestion.entities.Utilisateur;
import com.example.cabinetgestion.service.ServiceRdv;
import com.example.cabinetgestion.service.ServiceUtilisateur;
import com.example.cabinetgestion.service.DocumentService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class MedecinController {

    private final ServiceUtilisateur serviceUtilisateur;
    private final ServiceRdv serviceRdv;
    private final DocumentService documentService;

    // ========== PAGE D'ACCUEIL MÉDECIN ==========
    @GetMapping({"/home", "/medecin/home"})
    public String home(Model model,
                       Principal principal,
                       @RequestParam(required = false) String motCle) {

        // Récupérer le médecin connecté
        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        // RDV EN ATTENTE (nouveaux)
        List<Rdv> rdvEnAttente = serviceRdv.getRdvEnAttente(medecin.getId());

        // RDV du jour
        List<Rdv> rdvDuJour = serviceRdv.getRdvDuJour(medecin.getId());

        // Tous les RDV du médecin
        List<Rdv> mesRdv = serviceRdv.getRdvParMedecin(medecin.getId());

        // Liste des patients avec recherche
        List<Utilisateur> patients;
        if (motCle != null && !motCle.trim().isEmpty()) {
            patients = serviceUtilisateur.rechercherPatients(medecin.getId(), motCle.trim());
            model.addAttribute("motCle", motCle);
        } else {
            patients = serviceUtilisateur.getPatientsPourMedecin(medecin.getId());
        }

        // Pour chaque RDV, récupérer les documents du patient
        Map<Long, List<Document>> documentsParPatient = new HashMap<>();
        for (Rdv rdv : mesRdv) {
            Long patientId = rdv.getPatient().getId();
            if (!documentsParPatient.containsKey(patientId)) {
                List<Document> docs = documentService.getDocumentsByPatient(patientId);
                documentsParPatient.put(patientId, docs);
            }
        }

        // Ajouter tous les attributs au modèle
        model.addAttribute("rdvEnAttente", rdvEnAttente);
        model.addAttribute("rdvList", rdvDuJour);
        model.addAttribute("mesRdv", mesRdv);
        model.addAttribute("patientsList", patients);
        model.addAttribute("documentsParPatient", documentsParPatient);
        model.addAttribute("medecin", medecin);

        return "medecin/home";
    }

    // ========== GESTION DES RDV ==========

    // Formulaire d'ajout de RDV
    @GetMapping("/medecin/ajouterRdv")
    public String ajouterRdvForm(Model model, Principal principal) {

        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        List<Utilisateur> patients = serviceUtilisateur.getPatientsPourMedecin(medecin.getId());

        model.addAttribute("patients", patients);
        model.addAttribute("rdv", new Rdv());

        return "medecin/ajouterRdv";
    }

    // ✅ AJOUT DE RDV PAR LE MÉDECIN - CORRIGÉ
    @PostMapping("/medecin/ajouterRdv")
    public String ajouterRdvSubmit(@RequestParam("patientId") Long patientId,
                                   @RequestParam("dateRdv") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRdv,
                                   @RequestParam("heureRdv") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureRdv,
                                   @RequestParam(value = "motif", required = false) String motif,
                                   Principal principal) {

        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        Utilisateur patient = serviceUtilisateur.getUtilisateur(patientId);

        // ✅ Créer le RDV manuellement
        Rdv rdv = new Rdv();
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);
        rdv.setDateRdv(dateRdv);
        rdv.setHeureRdv(heureRdv);
        rdv.setMotif(motif);
        // PAS de setStatus() ici !

        // ✅ Utiliser la méthode dédiée (statut = ACCEPTE automatiquement)
        serviceRdv.saveRdvParMedecin(rdv);

        return "redirect:/medecin/home?success=rdvAdded";
    }

    // Formulaire de modification de RDV
    @GetMapping("/medecin/rdv/edit/{id}")
    public String editRdv(Model model, @PathVariable Long id, Principal principal) {

        Rdv rdv = serviceRdv.getRdv(id);

        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        List<Utilisateur> patients = serviceUtilisateur.getPatientsPourMedecin(medecin.getId());

        model.addAttribute("rdv", rdv);
        model.addAttribute("patients", patients);

        return "medecin/editRdv";
    }

    // ✅ MODIFICATION DE RDV PAR LE MÉDECIN - CORRIGÉ
    @PostMapping("/medecin/rdv/up")
    public String updateRdv(@RequestParam("id") Long id,
                            @RequestParam("patientId") Long patientId,
                            @RequestParam("dateRdv") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRdv,
                            @RequestParam("heureRdv") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureRdv,
                            @RequestParam(value = "motif", required = false) String motif,
                            Principal principal) {

        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        Utilisateur patient = serviceUtilisateur.getUtilisateur(patientId);

        // ✅ Récupérer le RDV existant
        Rdv rdv = serviceRdv.getRdv(id);
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);
        rdv.setDateRdv(dateRdv);
        rdv.setHeureRdv(heureRdv);
        rdv.setMotif(motif);
        // PAS de setStatus() ici !

        // ✅ Utiliser la méthode dédiée
        serviceRdv.saveRdvParMedecin(rdv);

        return "redirect:/medecin/home?success=rdvUpdated";
    }

    // ✅ ACCEPTER UN RDV
    @PostMapping("/medecin/accepter-rdv/{id}")
    public String accepterRdv(@PathVariable Long id) {
        serviceRdv.accepterRendezVous(id);
        return "redirect:/medecin/home?success=accepted";
    }

    // ✅ REFUSER UN RDV
    @PostMapping("/medecin/refuser-rdv/{id}")
    public String refuserRdv(@PathVariable Long id) {
        serviceRdv.refuserRendezVous(id);
        return "redirect:/medecin/home?success=refused";
    }

    // Suppression de RDV
    @PostMapping("/medecin/rdv/supprimer/{id}")
    public String supprimerRdv(@PathVariable Long id) {
        serviceRdv.supprimerRdvr(id);
        return "redirect:/medecin/home?success=rdvDeleted";
    }

    // ========== GESTION DES PATIENTS ==========

    // Formulaire de modification de patient
    @GetMapping("/medecin/patient/edit/{id}")
    public String editPatient(Model model, @PathVariable Long id) {
        Utilisateur patient = serviceUtilisateur.getUtilisateur(id);
        model.addAttribute("patient", patient);
        return "medecin/editPatient";
    }

    // Soumission de modification de patient
    @PostMapping("/medecin/patient/up")
    public String updatePatient(@ModelAttribute Utilisateur patient) {
        // Récupérer le patient existant pour garder certaines infos
        Utilisateur existing = serviceUtilisateur.getUtilisateur(patient.getId());

        patient.setRole(existing.getRole());
        patient.setMedecinCreateur(existing.getMedecinCreateur());

        // Si le mot de passe est vide, garder l'ancien
        if (patient.getMotDePasse() == null || patient.getMotDePasse().isEmpty()) {
            patient.setMotDePasse(existing.getMotDePasse());
        }

        serviceUtilisateur.saveUtilisateur(patient);
        return "redirect:/medecin/home?success=patientUpdated";
    }

    // Suppression de patient
    @PostMapping("/medecin/patient/supprimer/{id}")
    public String supprimerPatient(@PathVariable Long id) {
        serviceUtilisateur.supprimerUtilisateur(id);
        return "redirect:/medecin/home?success=patientDeleted";
    }

    // ========== RECHERCHE ==========

    // API de recherche de patients (optionnel - pour AJAX)
    @GetMapping("/medecin/patients/rechercher")
    @ResponseBody
    public List<Utilisateur> rechercherPatients(@RequestParam("motCle") String motCle,
                                                Principal principal) {

        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        if (motCle == null || motCle.trim().isEmpty()) {
            return serviceUtilisateur.getPatientsPourMedecin(medecin.getId());
        }

        return serviceUtilisateur.rechercherPatients(medecin.getId(), motCle.trim());
    }

    // ========== GESTION DES DOCUMENTS ==========

    @GetMapping("/medecin/telecharger-document/{documentId}")
    public ResponseEntity<Resource> telechargerDocument(@PathVariable Long documentId) {
        try {
            // Récupérer le document depuis la base de données
            Document document = documentService.getDocumentById(documentId);

            if (document == null) {
                return ResponseEntity.notFound().build();
            }

            // Charger le fichier
            Path filePath = Paths.get(document.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Déterminer le type de contenu
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Voir le document dans le navigateur
    @GetMapping("/medecin/voir-document/{documentId}")
    public ResponseEntity<Resource> voirDocument(@PathVariable Long documentId) {
        try {
            Document document = documentService.getDocumentById(documentId);

            if (document == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(document.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // inline au lieu de attachment pour afficher dans le navigateur
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + document.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}