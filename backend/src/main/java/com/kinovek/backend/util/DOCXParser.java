package com.kinovek.backend.util;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class DOCXParser {

    /**
     * Extracts all text content from a DOCX file.
     *
     * @param file the uploaded DOCX file
     * @return extracted text as a String
     * @throws IOException if the file cannot be read or parsed
     */
    public static String extractText(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             XWPFDocument document = new XWPFDocument(is)) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
            return text.toString().trim();
        }
    }
}
