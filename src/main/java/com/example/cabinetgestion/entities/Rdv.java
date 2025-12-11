package com.example.cabinetgestion.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Data
    public class Rdv {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private LocalDate dateRdv;

        private LocalTime heureRdv;

        private String motif;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false)
        private statusrdv status ;

        @ManyToOne
        @JoinColumn(name = "patient_id")
        private Utilisateur patient;

        @ManyToOne
        @JoinColumn(name = "medecin_id")
        private Utilisateur medecin;
    // Ajoutez cette relation si elle n'existe pas
    @OneToMany(mappedBy = "rdv", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ordonnance> ordonnances;



}
