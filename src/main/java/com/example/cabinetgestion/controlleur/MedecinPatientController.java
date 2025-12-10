package com.example.cabinetgestion.controlleur;

import com.example.cabinetgestion.entities.Role;
import com.example.cabinetgestion.entities.Utilisateur;
import com.example.cabinetgestion.service.ServiceUtilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/medecin/patient")
public class MedecinPatientController {

    private final ServiceUtilisateur serviceUtilisateur;


    @GetMapping("/ajouter")
    public String formAjout(@RequestParam(required = false) String returnTo,
                            Model model,
                            Principal principal) {


        Utilisateur medecin = serviceUtilisateur
                .getUtilisateursByEmail(principal.getName())
                .get(0);

        model.addAttribute("patient", new Utilisateur());
        model.addAttribute("returnTo", returnTo);
        model.addAttribute("medecinId", medecin.getId()); // Passer l'ID du médecin

        return "medecin/ajouter-patient";
    }


    @PostMapping("/save")
    public String savePatient(@ModelAttribute Utilisateur patient) {

        patient.setRole(Role.PATIENT);
        serviceUtilisateur.saveUtilisateur(patient);

        return "redirect:/medecin/home";
    }
}
