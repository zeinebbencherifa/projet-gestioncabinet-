package com.example.cabinetgestion.service;

import com.example.cabinetgestion.entities.Rdv;
import com.example.cabinetgestion.entities.Utilisateur;

import java.util.List;

public interface IserviceRdv {
    void saveRdv(Rdv rdv);

    // ✅ NOUVELLES MÉTHODES DÉDIÉES
    void saveRdvParMedecin(Rdv rdv);
    void saveRdvParPatient(Rdv rdv);

    void supprimerRdvr(Long id);
    Rdv getRdv(Long id);
    Rdv accepterRendezVous(Long idRdv);
    List<Rdv> getRdvEnAttente(Long idMedecin);
    Rdv refuserRendezVous(Long idRdv);
    List<Rdv> getRdvParMedecin(Long idMedecin);
    List<Rdv> getRdvParPatient(Long idPatient);
    List<Rdv> getRdvDuJour(Long idMedecin);
    List<Rdv> getAllRdv();
    List<Utilisateur> getPatientsDuMedecin(Long idMedecin);
}