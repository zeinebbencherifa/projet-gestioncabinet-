package com.example.cabinetgestion.controlleur;

import com.example.cabinetgestion.entities.Ordonnance;
import com.example.cabinetgestion.entities.Rdv;
import com.example.cabinetgestion.entities.Role;
import com.example.cabinetgestion.entities.Utilisateur;
import com.example.cabinetgestion.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/patient")
public class PatientController {

    private final ServiceUtilisateur serviceUtilisateur;
    private final DocumentService documentService;
    private final ServiceRdv serviceRdv;
    private final ServiceOrdonance serviceOrdonance;
    private final PdfService pdfService;

    // Modifier la méthode dashboard pour inclure les ordonnances
    @GetMapping({"/dashboard"})
    public String dashboard(Model model,
                            Principal principal,
                            @RequestParam(required = false) String motCle) {

        Utilisateur patient = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        List<Utilisateur> medecins = serviceUtilisateur.getAllMedecins();
        List<Rdv> mesRdv = serviceRdv.getRdvParPatient(patient.getId());


        List<Ordonnance> mesOrdonnances = serviceOrdonance.getOrdonnancesByPatient(patient.getId());

        model.addAttribute("medecins", medecins);
        model.addAttribute("mesRdv", mesRdv);
        model.addAttribute("mesOrdonnances", mesOrdonnances);
        model.addAttribute("patient", patient);

        return "patient/dashboard";
    }

    // Voir le détail d'une ordonnance
    @GetMapping("/ordonnance/{id}")
    public String voirOrdonnance(@PathVariable Long id,
                                 Model model,
                                 Principal principal) {

        Ordonnance ordonnance = serviceOrdonance.getOrdonnance(id);

        // Vérifier que l'ordonnance appartient bien au patient connecté
        Utilisateur patient = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        if (!ordonnance.getPatient().getId().equals(patient.getId())) {
            return "redirect:/patient/dashboard?error=unauthorized";
        }

        model.addAttribute("ordonnance", ordonnance);
        return "patient/detailOrdonnance";
    }

    @GetMapping("/medecins/rechercher")
    @ResponseBody
    public List<Utilisateur> rechercherMedecins(@RequestParam("motCle") String motCle) {
        if (motCle == null || motCle.trim().isEmpty()) {
            return serviceUtilisateur.getAllMedecins();
        }
        return serviceUtilisateur.rechercherMedecins(motCle);
    }



    @GetMapping("/prendre-rdv/{medecinId}")
    public String prendreRdvForm(@PathVariable Long medecinId, Model model, Principal principal) {

        Utilisateur medecin = serviceUtilisateur.getUtilisateur(medecinId);
        Utilisateur patient = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        model.addAttribute("medecin", medecin);
        model.addAttribute("patient", patient);
        model.addAttribute("rdv", new Rdv());

        return "patient/prendre-rdv";
    }

    @PostMapping("/prendre-rdv")
    public String prendreRdvSubmit(@ModelAttribute Rdv rdv,
                                   @RequestParam("medecinId") Long medecinId,
                                   @RequestParam(required = false) MultipartFile[] files,
                                   @RequestParam(required = false) String typeDocument,
                                   @RequestParam(required = false) String description,
                                   Principal principal) {

        Utilisateur patient = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        Utilisateur medecin = serviceUtilisateur.getUtilisateur(medecinId);

        rdv.setPatient(patient);
        rdv.setMedecin(medecin);

        serviceRdv.saveRdvParPatient(rdv);

        if (files != null && files.length > 0 && !files[0].isEmpty()) {
            documentService.saveDocumentsForPatient(
                    files,
                    typeDocument != null ? typeDocument : "AUTRE",
                    description != null ? description : "Document lié au RDV",
                    patient.getId()
            );
        }

        return "redirect:/patient/dashboard?success=rdv";
    }

    @PostMapping("/annuler-rdv/{id}")
    public String annulerRdv(@PathVariable Long id) {
        serviceRdv.supprimerRdvr(id);
        return "redirect:/patient/dashboard?success=cancelled";
    }
    @GetMapping("/ordonnances")
    public String listeOrdonnances(Model model, Principal principal) {
        Utilisateur patient = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        List<Ordonnance> ordonnances = serviceOrdonance.getOrdonnancesByPatient(patient.getId());

        model.addAttribute("ordonnances", ordonnances);
        model.addAttribute("patient", patient);

        return "patient/ordonnances";
    }





    @GetMapping("/ordonnance/telecharger/{id}")
    public ResponseEntity<byte[]> telechargerOrdonnancePdf(@PathVariable Long id,
                                                           Principal principal) {
        try {
            Ordonnance ordonnance = serviceOrdonance.getOrdonnance(id);


            Utilisateur patient = serviceUtilisateur
                    .getUtilisateursByEmail(principal.getName())
                    .get(0);

            if (!ordonnance.getPatient().getId().equals(patient.getId())) {
                return ResponseEntity.status(403).build();
            }

            // Générer le PDF
            byte[] pdfBytes = pdfService.genererPdfOrdonnance(ordonnance);

            // Nom du fichier
            String typeDoc = ordonnance.getType().toString().equals("ORDONNANCE") ? "Ordonnance" : "Rapport";
            //generer le nom du fichier
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