package com.example.cabinetgestion.service;

import com.example.cabinetgestion.entities.Role;
import com.example.cabinetgestion.entities.Utilisateur;
import com.example.cabinetgestion.repository.UtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
//userdetailservice pour charger les utilisateurs lors du login
public class ServiceUtilisateur implements IserviceUtilisateur, UserDetailsService {

    private UtilisateurRepository utilisateurRepository;
    private PasswordEncoder passwordEncoder;
    @Override
    public Utilisateur saveUtilisateur(Utilisateur utilisateur) {
        if (utilisateur.getId() == null || !utilisateur.getMotDePasse().startsWith("$2a$")) {
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        }
        return utilisateurRepository.save(utilisateur);
    }
    @Override
    public List<Utilisateur> getAllMedecins() {
        return utilisateurRepository.findByRole(Role.MEDECIN);
    }



    @Override
    public List<Utilisateur> rechercherPatients(Long idMedecin, String motCle) {
        String mc = "%" + motCle + "%";
        return utilisateurRepository.rechercherPatientsParMotcle(idMedecin, mc);
    }


    @Override
    public List<Utilisateur> rechercherMedecins(String motCle) {
        if (motCle == null || motCle.trim().isEmpty()) {
            return getAllMedecins();
        }
        String mc = "%" + motCle.trim() + "%";
        return utilisateurRepository.rechercherMedecinsParMotCle(mc);
    }

    @Override
    public void supprimerUtilisateur(Long id) {
        utilisateurRepository.deleteById(id);
    }

    @Override
    public Utilisateur getUtilisateur(Long id) {
        return utilisateurRepository.findById(id).orElse(null);
    }

    @Override
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    @Override
    public List<Utilisateur> getUtilisateursByEmail(String email) {
        return utilisateurRepository.getUtilisateurByEmail(email);
    }

    public List<Utilisateur> getAllPatients() {
        return utilisateurRepository.findByRole(Role.PATIENT);
    }
    //methode applle par springsecurity lorsque quelq'un esseye de se connecter
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //recherche de l'utulisateur via email
        List<Utilisateur> liste = utilisateurRepository.getUtilisateurByEmail(email);


        if (liste == null || liste.isEmpty()) {
            throw new UsernameNotFoundException("Utilisateur introuvable !");
        }

        Utilisateur utilisateur = liste.get(0);
        //construis un objet de type Spring Security (User)

        return User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .authorities(utilisateur.getRole().name())
                .build();
    }
    //patient creer par le medecin patient qui a pris rdv chez ce medecin

    @Override
    public List<Utilisateur> getPatientsPourMedecin(Long idMedecin) {
        List<Utilisateur> patientsCrees = utilisateurRepository.findByMedecinCreateurId(idMedecin);
        List<Utilisateur> patientsRdv = utilisateurRepository.findPatientsAvecRdv(idMedecin);
        //creer un hashset  un set il  garentit l'unicité

        Set<Utilisateur> fusion = new HashSet<>();
        //ajouter au set les patient creers
        fusion.addAll(patientsCrees);
        //ajouter au set les patients qui ont pris un rdv
        fusion.addAll(patientsRdv);
        //construire une liste a partir dub set

        return new ArrayList<>(fusion);
    }
}