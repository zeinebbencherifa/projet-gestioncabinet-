package com.example.cabinetgestion.entities;

;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Document  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String fileType;

    private String description;

    private String filePath; // chemin où le fichier est stocké dans /uploads/

    @Enumerated(EnumType.STRING)
    private TypeDocument typeDocument;

    private LocalDateTime uploadDate = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Utilisateur patient;


}
