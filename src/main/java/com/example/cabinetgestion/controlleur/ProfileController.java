package com.example.cabinetgestion.controlleur;

import com.example.cabinetgestion.entities.Utilisateur;
import com.example.cabinetgestion.service.ServiceFichier;
import com.example.cabinetgestion.service.ServiceUtilisateur;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@AllArgsConstructor
public class ProfileController {

    private final ServiceUtilisateur serviceUtilisateur;
    private final ServiceFichier serviceFichier;

    // Affiche la page profil
    @GetMapping("/profil")
    public String profil(Model model, @RequestParam Long userId) {
        Utilisateur utilisateur = serviceUtilisateur.getUtilisateur(userId);
        model.addAttribute("utilisateur", utilisateur);
        return "profil";
    }

    // Formulaire pour mettre à jour la photo de profil
    @PostMapping("/profil/upload")
    public String uploadPhoto(@RequestParam("photo") MultipartFile photo,
                              @RequestParam("userId") Long userId) throws IOException {

        // Sauvegarde le fichier
        String nomFichier = serviceFichier.sauvegarderFichier(photo);

        // Récupère l'utilisateur
        Utilisateur user = serviceUtilisateur.getUtilisateur(userId);

        // Met à jour la photo
        user.setPhoto(nomFichier);
        serviceUtilisateur.saveUtilisateur(user);

        return "redirect:/profil?userId=" + userId;
    }

    // Autres mises à jour du profil (nom, email...)
    @PostMapping("/profil/update")
    public String updateProfil(@RequestParam Long userId,
                               @RequestParam String nom,
                               @RequestParam String prenom,
                               @RequestParam String email) {

        Utilisateur user = serviceUtilisateur.getUtilisateur(userId);
        if (user != null) {
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            serviceUtilisateur.saveUtilisateur(user);
        }

        return "redirect:/profil?userId=" + userId;
    }

}
