package com.example.cabinetgestion.service;



import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ServiceFichier {

// Répertoire où les fichiers seront sauvegardés
private final Path dossierStockage = Paths.get("uploads");

public ServiceFichier() throws IOException {
    // Crée le dossier s'il n'existe pas
    if (!Files.exists(dossierStockage)) {
        Files.createDirectories(dossierStockage);
    }
}

// Méthode pour sauvegarder un fichier
public String sauvegarderFichier(MultipartFile fichier) throws IOException {
    if (fichier.isEmpty()) {
        throw new IOException("Fichier vide !");
    }

    // Nom du fichier
    String nomFichier = System.currentTimeMillis() + "_" + fichier.getOriginalFilename();

    Path cheminFichier = dossierStockage.resolve(nomFichier);
    Files.copy(fichier.getInputStream(), cheminFichier);

    return nomFichier; // retourne le nom du fichier pour l’enregistrer dans la DB
}

// Méthode pour récupérer le chemin d’un fichier
public Path getCheminFichier(String nomFichier) {
    return dossierStockage.resolve(nomFichier);
}
}


