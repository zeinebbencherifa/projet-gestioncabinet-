package com.example.cabinetgestion.service;

import com.example.cabinetgestion.entities.Ordonnance;

import java.util.List;

public interface IserviceOrdonance {
    Ordonnance creerOrdonnance(Ordonnance ordonnance);

    Ordonnance getOrdonnance(Long id);

    List<Ordonnance> getOrdonnancesByPatient(Long patientId);

    List<Ordonnance> getOrdonnancesByMedecin(Long medecinId);

    List<Ordonnance> getOrdonnancesByRdv(Long rdvId);

    void supprimerOrdonnance(Long id);
}
