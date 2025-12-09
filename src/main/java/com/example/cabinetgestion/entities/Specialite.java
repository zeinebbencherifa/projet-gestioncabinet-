package com.example.cabinetgestion.entities;



public enum Specialite {
    GENERALISTE("Médecin Généraliste"),
    CARDIOLOGUE("Cardiologue"),
    DERMATOLOGUE("Dermatologue"),
    PEDIATRE("Pédiatre"),
    GYNECO_OBSTETRIQUE("Gynécologue-Obstétricien"),
    ORL("ORL (Oto-Rhino-Laryngologiste)"),
    OPHTALMOLOGUE("Ophtalmologue"),
    DENTISTE("Dentiste"),
    PSYCHIATRE("Psychiatre"),
    RADIOLOGUE("Radiologue"),
    ANESTHESISTE("Anesthésiste-Réanimateur"),
    CHIRURGIEN("Chirurgien"),
    NEUROLOGUE("Neurologue"),
    ENDOCRINOLOGUE("Endocrinologue"),
    RHUMATOLOGUE("Rhumatologue"),
    PNEUMOLOGUE("Pneumologue"),
    UROLOGUE("Urologue"),
    NEPHROLOGUE("Néphrologue"),
    GASTRO_ENTEROLOGUE("Gastro-entérologue"),
    AUTRE("Autre");

    private final String label;

    Specialite(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
