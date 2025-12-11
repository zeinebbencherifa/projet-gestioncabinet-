package com.example.cabinetgestion.controlleur;

import com.example.cabinetgestion.entities.*;
import com.example.cabinetgestion.service.*;
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
import org.springframework.web.multipart.MultipartFile;

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
@RequestMapping("/medecin")
public class MedecinController {

    private final ServiceUtilisateur serviceUtilisateur;
    private final ServiceRdv serviceRdv;
    private final DocumentService documentService;
    private final ServiceOrdonance serviceOrdonance;
    private final PdfService pdfService;


    // page principale du medecin
    @GetMapping({"/home", "/medecin/home"})
    public String home(Model model,
                       Principal principal,
                       @RequestParam(required = false) String motCle) {

        // Récupérer le médecin connecte
        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        List<Rdv> rdvEnAttente = serviceRdv.getRdvEnAttente(medecin.getId());

        List<Rdv> rdvDuJour = serviceRdv.getRdvDuJour(medecin.getId());

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
        //creer un map avec qui récupere les documents des patients
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
    @PostMapping("/save")
    public String savePatient(
            @ModelAttribute Utilisateur patient,
            @RequestParam(required = false) String returnTo,
            Principal principal) {
        patient.setRole(Role.PATIENT);
        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        patient.setMedecinCreateur(medecin);
        Utilisateur savedPatient = serviceUtilisateur.saveUtilisateur(patient);
        if ("rdv".equals(returnTo)) {
            return "redirect:/medecin/ajouterRdv?success=patientCreated";
        }

        return "redirect:/medecin/home?success=patientSaved";
    }


    // Formulaire d'ajout des RDV
    @GetMapping("/ajouterRdv")
    //principal hiya teebaa   springsecurity pour montrer  l'utilisateur connecte
    public String ajouterRdvForm(Model model, Principal principal) {
//traj3lek l'utulisateur connecter
        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);
        //trajaalek liste des patients lel medcins heka

        List<Utilisateur> patients = serviceUtilisateur.getPatientsPourMedecin(medecin.getId());

        model.addAttribute("patients", patients);
        model.addAttribute("rdv", new Rdv());

        return "medecin/ajouterRdv";
    }


    @PostMapping("/ajouterRdv")
    public String ajouterRdvSubmit(@RequestParam("patientId") Long patientId,
                                   @RequestParam("dateRdv") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateRdv,
                                   @RequestParam("heureRdv") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heureRdv,
                                   @RequestParam(value = "motif", required = false) String motif,
                                   Principal principal) {

        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        Utilisateur patient = serviceUtilisateur.getUtilisateur(patientId);

        //on creer un rdv manuellement car le statut lezm ykoun définis comme accepter
        Rdv rdv = new Rdv();
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);
        rdv.setDateRdv(dateRdv);
        rdv.setHeureRdv(heureRdv);
        rdv.setMotif(motif);


        serviceRdv.saveRdvParMedecin(rdv);

        return "redirect:/medecin/home?success=rdvAdded";
    }

    // Formulaire de modification de RDV
    @GetMapping("/rdv/edit/{id}")
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

    //
    @PostMapping("/rdv/up")
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
        Rdv rdv = serviceRdv.getRdv(id);
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);
        rdv.setDateRdv(dateRdv);
        rdv.setHeureRdv(heureRdv);
        rdv.setMotif(motif);

        serviceRdv.saveRdvParMedecin(rdv);

        return "redirect:/medecin/home?success=rdvUpdated";
    }


    @PostMapping("/accepter-rdv/{id}")
    public String accepterRdv(@PathVariable Long id) {
        serviceRdv.accepterRendezVous(id);
        return "redirect:/medecin/home?success=accepted";
    }


    @PostMapping("/refuser-rdv/{id}")
    public String refuserRdv(@PathVariable Long id) {
        serviceRdv.refuserRendezVous(id);
        return "redirect:/medecin/home?success=refused";
    }


    @PostMapping("/rdv/supprimer/{id}")
    public String supprimerRdv(@PathVariable Long id) {
        serviceRdv.supprimerRdvr(id);
        return "redirect:/medecin/home?success=rdvDeleted";
    }


    @GetMapping("/patient/edit/{id}")
    public String editPatient(Model model, @PathVariable Long id) {
        Utilisateur patient = serviceUtilisateur.getUtilisateur(id);
        model.addAttribute("patient", patient);
        return "medecin/editPatient";
    }

    @PostMapping("/patient/up")
    public String updatePatient(@ModelAttribute Utilisateur patient) {
        // Récuperer  le patient existant pour garder lesinfos
        Utilisateur existing = serviceUtilisateur.getUtilisateur(patient.getId());
//le role ne soit pas etre changer donc on le set nous meme, et le medecins créateur
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
    @PostMapping("/patient/supprimer/{id}")
    public String supprimerPatient(@PathVariable Long id) {
        serviceUtilisateur.supprimerUtilisateur(id);
        return "redirect:/medecin/home?success=patientDeleted";
    }




    @GetMapping("/patients/rechercher")
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



    @GetMapping("/telecharger-document/{documentId}")
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
//envoye une reponse http 200 0OK
            return ResponseEntity.ok()
                    //declarer le type du fichier
                    //converti le content type en objetspring mediatype

                    .contentType(MediaType.parseMediaType(contentType))
                    //indication pour le nab=vigateur pour qu'il telacherger le document avec le nom du fichier
                    //attachment hiya eli tkhalina netachergiw
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getFileName() + "\"")
                    //le cors est le fichier réél envoyer par la ressource
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Voir le document dans le navigateur
    @GetMapping("/voir-document/{documentId}")
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

            // inline besh najmou nchoufouh ala navigateur
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + document.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    // Formulaire de création d'ordonnance
    @GetMapping("/ordonnance/creer")
    public String creerOrdonnanceForm(@RequestParam(required = false) Long rdvId,
                                      @RequestParam(required = false) Long patientId,
                                      Model model,
                                      Principal principal) {

        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        Ordonnance ordonnance = new Ordonnance();
        ordonnance.setMedecin(medecin);

        // Si on vient d'un rdv
        if (rdvId != null) {
            Rdv rdv = serviceRdv.getRdv(rdvId);
            ordonnance.setRdv(rdv);
            ordonnance.setPatient(rdv.getPatient());
            model.addAttribute("rdv", rdv);
        }
        // Si on sélectionne directement un patient
        else if (patientId != null) {
            Utilisateur patient = serviceUtilisateur.getUtilisateur(patientId);
            ordonnance.setPatient(patient);
        }

        // Liste des patients pour sélection
        List<Utilisateur> patients = serviceUtilisateur.getPatientsPourMedecin(medecin.getId());

        model.addAttribute("ordonnance", ordonnance);
        model.addAttribute("patients", patients);

        return "medecin/creerOrdonnance";
    }
    @PostMapping("/ordonnance/save")
    public String saveOrdonnance(@ModelAttribute Ordonnance ordonnance,
                                 @RequestParam("patientId") Long patientId,
                                 @RequestParam(required = false) Long rdvId,
                                 Principal principal) {

        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        Utilisateur patient = serviceUtilisateur.getUtilisateur(patientId);

        ordonnance.setMedecin(medecin);
        ordonnance.setPatient(patient);

        if (rdvId != null) {
            Rdv rdv = serviceRdv.getRdv(rdvId);
            ordonnance.setRdv(rdv);
        }

        serviceOrdonance.creerOrdonnance(ordonnance);

        // Redirection vers la page d'accueil avec message de succès
        return "redirect:/medecin/home?success=ordonnanceCreated";
    }



    // Voir les ordonnances d'un patient
    @GetMapping("/patient/{patientId}/ordonnances")
    public String voirOrdonnancesPatient(@PathVariable Long patientId,
                                         Model model,
                                         Principal principal) {

        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        Utilisateur patient = serviceUtilisateur.getUtilisateur(patientId);
        List<Ordonnance> ordonnances = serviceOrdonance.getOrdonnancesByPatient(patientId);

        model.addAttribute("patient", patient);
        model.addAttribute("ordonnances", ordonnances);
        model.addAttribute("medecin", medecin);

        return "medecin/ordonnancesPatient";
    }

    // Voir le détail d'une ordonnance
    @GetMapping("/ordonnance/{id}")
    public String voirOrdonnance(@PathVariable Long id, Model model) {
        Ordonnance ordonnance = serviceOrdonance.getOrdonnance(id);
        model.addAttribute("ordonnance", ordonnance);
        return "medecin/detailOrdonnance";
    }

    // Supprimer une ordonnance
    @PostMapping("/ordonnance/supprimer/{id}")
    public String supprimerOrdonnance(@PathVariable Long id) {
        serviceOrdonance.supprimerOrdonnance(id);
        return "redirect:/medecin/home?success=ordonnanceDeleted";
    }





    @GetMapping("/ordonnance/telecharger/{id}")
    public ResponseEntity<byte[]> telechargerOrdonnancePdf(@PathVariable Long id,
                                                           Principal principal) {
        try {
            Ordonnance ordonnance = serviceOrdonance.getOrdonnance(id);

            // Vérifier que l'ordonnance appartient bien au médecin connecté
            Utilisateur medecin = serviceUtilisateur
                    .getUtilisateursByEmail(principal.getName())
                    .get(0);

            if (!ordonnance.getMedecin().getId().equals(medecin.getId())) {
                return ResponseEntity.status(403).build();
            }

            // Générer le PDF
            byte[] pdfBytes = pdfService.genererPdfOrdonnance(ordonnance);

            // Nom du fichier
            String typeDoc = ordonnance.getType().toString().equals("ORDONNANCE") ? "Ordonnance" : "Rapport";
            String fileName = typeDoc + "_" + ordonnance.getPatient().getNom() + "_" +
                    ordonnance.getDateCreation().format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy")) + ".pdf";

            // Configuration des headers HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}