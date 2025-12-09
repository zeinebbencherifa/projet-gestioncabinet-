package com.example.cabinetgestion.repository;

import com.example.cabinetgestion.entities.Rdv;
import com.example.cabinetgestion.entities.Utilisateur;
import com.example.cabinetgestion.entities.statusrdv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RdvRepository extends JpaRepository<Rdv, Long> {

    List<Rdv> findByMedecin(Utilisateur medecin);

    List<Rdv> findByPatient(Utilisateur patient);

    List<Rdv> findByMedecinIdAndDateRdv(Long idMedecin, LocalDate date);
    List<Rdv> findByMedecinIdAndStatus(Long idMedecin, statusrdv status);

    // ➤ Liste UNIQUE des patients qui ont pris RDV avec un médecin
    @Query("SELECT DISTINCT r.patient FROM Rdv r WHERE r.medecin.id = :idMedecin")
    List<Utilisateur> findPatientsByMedecin(Long idMedecin);
}

