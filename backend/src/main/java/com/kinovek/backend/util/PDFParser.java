package com.kinovek.backend.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class PDFParser {

    /**
     * Extracts all text content from a PDF file.
     *
     * @param file the uploaded PDF file
     * @return extracted text as a String
     * @throws IOException if the file cannot be read or parsed
     */
    public static String extractText(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
