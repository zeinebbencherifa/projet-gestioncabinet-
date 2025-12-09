package com.example.cabinetgestion.repository;

import com.example.cabinetgestion.entities.Role;
import com.example.cabinetgestion.entities.Specialite;
import com.example.cabinetgestion.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    List<Utilisateur> getUtilisateurByEmail(String email);

    List<Utilisateur> findByRole(Role role);

    @Query("SELECT u FROM Utilisateur u WHERE u.medecinCreateur.id = :idMedecin " +
            "AND (u.nom LIKE :mc OR u.prenom LIKE :mc OR u.email LIKE :mc)")
    List<Utilisateur> rechercherPatientsParMotcle(@Param("idMedecin") Long idMedecin,
                                                  @Param("mc") String mc);

    @Query("SELECT u FROM Utilisateur u WHERE u.medecinCreateur.id = :idMedecin")
    List<Utilisateur> findByMedecinCreateurId(@Param("idMedecin") Long idMedecin);

    @Query("SELECT DISTINCT r.patient FROM Rdv r WHERE r.medecin.id = :idMedecin")
    List<Utilisateur> findPatientsAvecRdv(@Param("idMedecin") Long idMedecin);

    // ✅ NOUVELLE MÉTHODE : Recherche de médecins par nom ou spécialité
    @Query("SELECT u FROM Utilisateur u WHERE u.role = 'MEDECIN' " +
            "AND (LOWER(u.nom) LIKE LOWER(:mc) " +
            "OR LOWER(u.prenom) LIKE LOWER(:mc) " +
            "OR LOWER(CAST(u.specialite AS string)) LIKE LOWER(:mc))")
    List<Utilisateur> rechercherMedecinsParMotCle(@Param("mc") String mc);
}