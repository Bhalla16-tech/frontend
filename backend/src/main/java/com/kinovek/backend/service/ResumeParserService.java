package com.kinovek.backend.service;

import com.kinovek.backend.util.DOCXParser;
import com.kinovek.backend.util.PDFParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ResumeParserService {

    /**
     * Parses a resume file and extracts text content.
     * Supports PDF and DOCX formats only.
     *
     * @param file the uploaded resume file
     * @return extracted text content
     * @throws IOException if the file cannot be parsed
     * @throws IllegalArgumentException if the file type is not supported
     */
    public String parseResume(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name is missing");
        }

        String lowerName = fileName.toLowerCase();

        if (lowerName.endsWith(".pdf")) {
            return PDFParser.extractText(file);
        } else if (lowerName.endsWith(".docx")) {
            return DOCXParser.extractText(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type. Only PDF and DOCX files are supported.");
        }
    }
}
