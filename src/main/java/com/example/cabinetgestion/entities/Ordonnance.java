package com.example.cabinetgestion.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ordonnance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medecin_id", nullable = false)
    private Utilisateur medecin;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Utilisateur patient;

    @ManyToOne
    @JoinColumn(name = "rdv_id")
    private Rdv rdv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDocument type; // ORDONNANCE ou RAPPORT

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column(columnDefinition = "TEXT")
    private String diagnostic;

    @Column(columnDefinition = "TEXT")
    private String contenu; // Médicaments ou observations

    @Column(columnDefinition = "TEXT")
    private String recommandations;

    private String observations;

    public enum TypeDocument {
        ORDONNANCE,
        RAPPORT_MEDICAL
    }
}