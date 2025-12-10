package com.example.cabinetgestion.controlleur;

import com.example.cabinetgestion.entities.Rdv;
import com.example.cabinetgestion.entities.Role;
import com.example.cabinetgestion.entities.Utilisateur;
import com.example.cabinetgestion.service.ServiceRdv;
import com.example.cabinetgestion.service.DocumentService;
import com.example.cabinetgestion.service.ServiceUtilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/patient")
public class PatientController {

    private final ServiceUtilisateur serviceUtilisateur;
    private final DocumentService documentService;
    private final ServiceRdv serviceRdv;


    @GetMapping({"/dashboard"})
    public String dashboard(Model model,
                            Principal principal,
                            @RequestParam(required = false) String motCle) {

        Utilisateur patient = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);


        List<Utilisateur> medecins = serviceUtilisateur.getAllMedecins();
        model.addAttribute("medecins", medecins);


        // RDV du patient
        List<Rdv> mesRdv = serviceRdv.getRdvParPatient(patient.getId());

        model.addAttribute("medecins", medecins);
        model.addAttribute("mesRdv", mesRdv);
        model.addAttribute("patient", patient);

        return "patient/dashboard";
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


}