package com.example.cabinetgestion.service;

import com.example.cabinetgestion.entities.Rdv;
import com.example.cabinetgestion.entities.Utilisateur;
import com.example.cabinetgestion.entities.statusrdv;
import com.example.cabinetgestion.repository.RdvRepository;
import com.example.cabinetgestion.repository.UtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class ServiceRdv implements IserviceRdv {
    private final UtilisateurRepository utilisateurRepository;
    private final RdvRepository rdvRepository;

    @Override
    public void saveRdv(Rdv rdv) {
        // Méthode générique - conserve la logique actuelle pour les modifications
        if (rdv.getId() == null && rdv.getStatus() == null) {
            rdv.setStatus(statusrdv.EN_ATTENTE);
        } else if (rdv.getId() != null && rdv.getStatus() == null) {
            Rdv existingRdv = rdvRepository.findById(rdv.getId()).orElse(null);
            if (existingRdv != null) {
                rdv.setStatus(existingRdv.getStatus());
            }
        }
        rdvRepository.save(rdv);
    }

    // ✅ NOUVELLE MÉTHODE : RDV créé par le MÉDECIN
    @Override
    public void saveRdvParMedecin(Rdv rdv) {
        // Pour un nouveau RDV créé par le médecin → ACCEPTE automatiquement
        if (rdv.getId() == null) {
            rdv.setStatus(statusrdv.ACCEPTE);
        } else {
            // Si c'est une modification, garder le statut existant ou le nouveau
            if (rdv.getStatus() == null) {
                Rdv existingRdv = rdvRepository.findById(rdv.getId()).orElse(null);
                if (existingRdv != null) {
                    rdv.setStatus(existingRdv.getStatus());
                }
            }
        }
        rdvRepository.save(rdv);
    }

    // ✅ NOUVELLE MÉTHODE : RDV créé par le PATIENT
    @Override
    public void saveRdvParPatient(Rdv rdv) {
        // Pour un nouveau RDV créé par le patient → EN_ATTENTE
        if (rdv.getId() == null) {
            rdv.setStatus(statusrdv.EN_ATTENTE);
        } else {
            // Si c'est une modification, garder le statut existant
            if (rdv.getStatus() == null) {
                Rdv existingRdv = rdvRepository.findById(rdv.getId()).orElse(null);
                if (existingRdv != null) {
                    rdv.setStatus(existingRdv.getStatus());
                }
            }
        }
        rdvRepository.save(rdv);
    }

    @Override
    public void supprimerRdvr(Long id) {
        rdvRepository.deleteById(id);
    }

    @Override
    public Rdv getRdv(Long id) {
        return rdvRepository.findById(id).orElseThrow(() ->
                new RuntimeException("RDV introuvable avec l'ID: " + id));
    }

    @Override
    public Rdv accepterRendezVous(Long idRdv) {
        Rdv rdv = rdvRepository.findById(idRdv)
                .orElseThrow(() -> new RuntimeException("RDV introuvable avec l'ID: " + idRdv));

        rdv.setStatus(statusrdv.ACCEPTE);
        return rdvRepository.save(rdv);
    }

    @Override
    public List<Rdv> getRdvEnAttente(Long idMedecin) {
        return rdvRepository.findByMedecinIdAndStatus(idMedecin, statusrdv.EN_ATTENTE);
    }

    @Override
    public Rdv refuserRendezVous(Long idRdv) {
        Rdv rdv = rdvRepository.findById(idRdv)
                .orElseThrow(() -> new RuntimeException("RDV introuvable avec l'ID: " + idRdv));

        rdv.setStatus(statusrdv.REFUSE);
        return rdvRepository.save(rdv);
    }

    @Override
    public List<Rdv> getRdvParMedecin(Long idMedecin) {
        Utilisateur medecin = utilisateurRepository.findById(idMedecin)
                .orElseThrow(() -> new RuntimeException("Médecin introuvable avec l'ID: " + idMedecin));

        return rdvRepository.findByMedecin(medecin);
    }

    @Override
    public List<Rdv> getRdvParPatient(Long idPatient) {
        Utilisateur patient = utilisateurRepository.findById(idPatient)
                .orElseThrow(() -> new RuntimeException("Patient introuvable avec l'ID: " + idPatient));

        return rdvRepository.findByPatient(patient);
    }

    @Override
    public List<Rdv> getRdvDuJour(Long idMedecin) {
        LocalDate today = LocalDate.now();
        return rdvRepository.findByMedecinIdAndDateRdv(idMedecin, today);
    }

    @Override
    public List<Rdv> getAllRdv() {
        return rdvRepository.findAll();
    }

    @Override
    public List<Utilisateur> getPatientsDuMedecin(Long idMedecin) {
        return rdvRepository.findPatientsByMedecin(idMedecin);
    }
}