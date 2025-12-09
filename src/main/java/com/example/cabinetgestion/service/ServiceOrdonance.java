package com.example.cabinetgestion.service;
import com.example.cabinetgestion.entities.Ordonnance;
import com.example.cabinetgestion.repository.OrdonnanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class ServiceOrdonance implements IserviceOrdonance{


        private final OrdonnanceRepository ordonnanceRepository;
        @Override
        public Ordonnance creerOrdonnance(Ordonnance ordonnance) {
            ordonnance.setDateCreation(LocalDateTime.now());
            return ordonnanceRepository.save(ordonnance);
        }
@Override
public Ordonnance getOrdonnance(Long id) {
            return ordonnanceRepository.findById(id).orElse(null);
        }
@Override
public List<Ordonnance> getOrdonnancesByPatient(Long patientId) {
            return ordonnanceRepository.findByPatientIdOrderByDateCreationDesc(patientId);
        }
@Override
public List<Ordonnance> getOrdonnancesByMedecin(Long medecinId) {
            return ordonnanceRepository.findByMedecinIdOrderByDateCreationDesc(medecinId);
        }
        @Override
        public List<Ordonnance> getOrdonnancesByRdv(Long rdvId) {
            return ordonnanceRepository.findByRdvId(rdvId);
        }
@Override
public void supprimerOrdonnance(Long id) {
            ordonnanceRepository.deleteById(id);
        }
    }

