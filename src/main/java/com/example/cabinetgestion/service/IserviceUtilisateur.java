package com.example.cabinetgestion.service;

import com.example.cabinetgestion.entities.Utilisateur;
import java.util.List;

public interface IserviceUtilisateur {

    List<Utilisateur> getAllMedecins();

    Utilisateur saveUtilisateur(Utilisateur utilisateur);

    List<Utilisateur> rechercherPatients(Long idMedecin, String motCle);

    void supprimerUtilisateur(Long id);

    Utilisateur getUtilisateur(Long id);

    List<Utilisateur> getAllPatients();

    List<Utilisateur> getAllUtilisateurs();

    List<Utilisateur> getUtilisateursByEmail(String email);

    List<Utilisateur> getPatientsPourMedecin(Long idMedecin);
    List<Utilisateur> rechercherMedecins(String motCle);
}
