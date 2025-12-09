package com.example.cabinetgestion.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String photo;
    @Enumerated(EnumType.STRING)
    private Specialite specialite;

    @Enumerated(EnumType.STRING)
    private Role role;


    @ManyToOne
    @JoinColumn(name = "medecin_id")
    private Utilisateur medecinCreateur;
}
