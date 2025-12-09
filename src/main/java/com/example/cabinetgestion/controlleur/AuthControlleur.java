package com.example.cabinetgestion.controlleur;

import com.example.cabinetgestion.entities.Role;
import com.example.cabinetgestion.entities.Specialite;
import com.example.cabinetgestion.entities.Utilisateur;
import com.example.cabinetgestion.service.ServiceUtilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthControlleur {

    @Autowired
    private ServiceUtilisateur serviceUtilisateur;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String nom,
                           @RequestParam String prenom,
                           @RequestParam String email,
                           @RequestParam String motDePasse,
                           @RequestParam Role role,
                           @RequestParam(required = false) Specialite specialite) {

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse(motDePasse);
        utilisateur.setRole(role);


        if (role == Role.MEDECIN && specialite != null) {
            utilisateur.setSpecialite(specialite);
        }

        serviceUtilisateur.saveUtilisateur(utilisateur);

        return "redirect:/login?success=registered";
    }
    @GetMapping("/redirect")
    public String redirectAfterLogin() {
//récupère l’objet Authentication de Spring Security(l'email, le role, les permissions )
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("MEDECIN"))) {
            return "redirect:/medecin/home";
        }

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("PATIENT"))) {
            return "redirect:/patient/home";
        }

        return "redirect:/";
    }
}
