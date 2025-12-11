package com.example.cabinetgestion.service;



import com.example.cabinetgestion.entities.Ordonnance;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    public byte[] genererPdfOrdonnance(Ordonnance ordonnance) throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Police avec support des caractères spéciaux
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bf, 20, Font.BOLD, BaseColor.BLUE);
            Font headerFont = new Font(bf, 14, Font.BOLD, BaseColor.BLACK);
            Font normalFont = new Font(bf, 12, Font.NORMAL, BaseColor.BLACK);
            Font smallFont = new Font(bf, 10, Font.NORMAL, BaseColor.GRAY);

            // En-tête du document
            Paragraph title = new Paragraph();
            if ("ORDONNANCE".equals(ordonnance.getType().toString())) {
                title.add(new Chunk("ORDONNANCE MEDICALE", titleFont));
            } else {
                title.add(new Chunk("RAPPORT MEDICAL", titleFont));
            }
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Date de création
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a' HH:mm");
            Paragraph date = new Paragraph("Cree le : " + ordonnance.getDateCreation().format(formatter), smallFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(30);
            document.add(date);

            // Ligne de séparation
            document.add(new Paragraph("_________________________________________________________________"));
            document.add(Chunk.NEWLINE);

            // Informations du patient
            document.add(new Paragraph("PATIENT", headerFont));
            document.add(new Paragraph("Nom : " + ordonnance.getPatient().getPrenom() + " " + ordonnance.getPatient().getNom(), normalFont));
            document.add(new Paragraph("Email : " + ordonnance.getPatient().getEmail(), normalFont));
            document.add(Chunk.NEWLINE);

            // Diagnostic
            if (ordonnance.getDiagnostic() != null && !ordonnance.getDiagnostic().isEmpty()) {
                document.add(new Paragraph("DIAGNOSTIC", headerFont));
                document.add(new Paragraph(ordonnance.getDiagnostic(), normalFont));
                document.add(Chunk.NEWLINE);
            }

            // Contenu (Prescription ou Observations)
            if (ordonnance.getContenu() != null && !ordonnance.getContenu().isEmpty()) {
                if ("ORDONNANCE".equals(ordonnance.getType().toString())) {
                    document.add(new Paragraph("PRESCRIPTION", headerFont));
                } else {
                    document.add(new Paragraph("OBSERVATIONS MEDICALES", headerFont));
                }
                document.add(new Paragraph(ordonnance.getContenu(), normalFont));
                document.add(Chunk.NEWLINE);
            }

            // Recommandations
            if (ordonnance.getRecommandations() != null && !ordonnance.getRecommandations().isEmpty()) {
                document.add(new Paragraph("RECOMMANDATIONS", headerFont));
                document.add(new Paragraph(ordonnance.getRecommandations(), normalFont));
                document.add(Chunk.NEWLINE);
            }

            // Observations supplémentaires
            if (ordonnance.getObservations() != null && !ordonnance.getObservations().isEmpty()) {
                document.add(new Paragraph("OBSERVATIONS SUPPLEMENTAIRES", headerFont));
                document.add(new Paragraph(ordonnance.getObservations(), normalFont));
                document.add(Chunk.NEWLINE);
            }

            // Signature du médecin
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("_________________________________________________________________"));
            Paragraph signature = new Paragraph();
            signature.add(new Chunk("Le medecin prescripteur,\n", normalFont));
            signature.add(new Chunk("Dr. " + ordonnance.getMedecin().getPrenom() + " " + ordonnance.getMedecin().getNom() + "\n", headerFont));
            signature.add(new Chunk(ordonnance.getMedecin().getEmail(), smallFont));
            signature.setAlignment(Element.ALIGN_RIGHT);
            document.add(signature);

        } finally {
            document.close();
        }

        return baos.toByteArray();
    }
}