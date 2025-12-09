package com.example.cabinetgestion.repository;


import com.example.cabinetgestion.entities.Ordonnance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrdonnanceRepository extends JpaRepository<Ordonnance, Long> {

    List<Ordonnance> findByPatientIdOrderByDateCreationDesc(Long patientId);

    List<Ordonnance> findByMedecinIdOrderByDateCreationDesc(Long medecinId);

    List<Ordonnance> findByRdvId(Long rdvId);

    @Query("SELECT o FROM Ordonnance o WHERE o.medecin.id = :medecinId " +
            "AND o.patient.id = :patientId ORDER BY o.dateCreation DESC")
    List<Ordonnance> findByMedecinAndPatient(@Param("medecinId") Long medecinId,
                                             @Param("patientId") Long patientId);
}