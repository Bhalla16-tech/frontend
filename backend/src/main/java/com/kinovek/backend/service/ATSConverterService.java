package com.kinovek.backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ATSConverterService {

    @Autowired
    private ResumeParserService resumeParserService;

    // Standard ATS-friendly section headers
    private static final Map<String, String> SECTION_HEADER_MAP = new LinkedHashMap<>();
    static {
        SECTION_HEADER_MAP.put("summary|objective|profile|about me|professional summary|career objective", "PROFESSIONAL SUMMARY");
        SECTION_HEADER_MAP.put("experience|work experience|employment|professional experience|work history", "WORK EXPERIENCE");
        SECTION_HEADER_MAP.put("education|academic|qualifications|academic background", "EDUCATION");
        SECTION_HEADER_MAP.put("skills|technical skills|core competencies|key skills|proficiencies", "SKILLS");
        SECTION_HEADER_MAP.put("certifications|certificates|licenses|credentials", "CERTIFICATIONS");
        SECTION_HEADER_MAP.put("projects|key projects|notable projects", "PROJECTS");
        SECTION_HEADER_MAP.put("awards|honors|achievements|accomplishments", "AWARDS & ACHIEVEMENTS");
        SECTION_HEADER_MAP.put("languages|language proficiency", "LANGUAGES");
        SECTION_HEADER_MAP.put("references", "REFERENCES");
    }

    // Patterns to detect non-ATS-friendly content
    private static final Pattern TABLE_PATTERN = Pattern.compile("\\|.*\\|.*\\|", Pattern.MULTILINE);
    private static final Pattern MULTIPLE_COLUMNS = Pattern.compile("\\t{2,}|\\s{4,}(?=\\S+\\s{4,}\\S+)");
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[\u2022\u25CF\u25CB\u25AA\u25AB\u2023\u2043]");
    private static final Pattern MULTIPLE_BLANK_LINES = Pattern.compile("\n{3,}");

    /**
     * Converts a resume to an ATS-friendly PDF.
     *
     * @param resumeFile the uploaded resume (PDF or DOCX)
     * @return byte array of the clean ATS-compliant PDF
     */
    public byte[] convertToATSFriendly(MultipartFile resumeFile) throws IOException {
        // 1. Parse the uploaded resume
        String rawText = resumeParserService.parseResume(resumeFile);

        // 2. Clean and normalize the text
        String cleanedText = cleanText(rawText);

        // 3. Normalize section headers
        cleanedText = normalizeSectionHeaders(cleanedText);

        // 4. Convert to linear structure
        cleanedText = linearize(cleanedText);

        // 5. Generate clean ATS-compliant PDF
        return generatePDF(cleanedText);
    }

    /**
     * Removes tables, images references, multi-column layouts, and special characters.
     */
    private String cleanText(String text) {
        // Remove table-like structures
        text = TABLE_PATTERN.matcher(text).replaceAll("");

        // Collapse multi-column layouts into single column
        text = MULTIPLE_COLUMNS.matcher(text).replaceAll("\n");

        // Replace decorative bullets with standard dash
        text = SPECIAL_CHARS.matcher(text).replaceAll("-");

        // Remove image references
        text = text.replaceAll("(?i)\\[image[^]]*]", "");
        text = text.replaceAll("(?i)<img[^>]*>", "");

        // Remove HTML tags if any
        text = text.replaceAll("<[^>]+>", "");

        // Normalize whitespace
        text = text.replaceAll("\t", " ");
        text = text.replaceAll(" {2,}", " ");

        // Collapse excessive blank lines
        text = MULTIPLE_BLANK_LINES.matcher(text).replaceAll("\n\n");

        return text.trim();
    }

    /**
     * Normalizes section headers to standard ATS-recognized names.
     */
    private String normalizeSectionHeaders(String text) {
        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            String normalized = matchSectionHeader(trimmed);
            if (normalized != null) {
                result.append("\n").append(normalized).append("\n");
            } else {
                result.append(line).append("\n");
            }
        }

        return result.toString().trim();
    }

    /**
     * Matches a line against known section header patterns.
     */
    private String matchSectionHeader(String line) {
        // Remove common decorations from potential headers
        String cleaned = line.replaceAll("^[\\-=_*#:]+", "").replaceAll("[\\-=_*#:]+$", "").trim();
        if (cleaned.isEmpty() || cleaned.length() > 50) return null;

        String lower = cleaned.toLowerCase();
        for (Map.Entry<String, String> entry : SECTION_HEADER_MAP.entrySet()) {
            String[] aliases = entry.getKey().split("\\|");
            for (String alias : aliases) {
                if (lower.equals(alias.trim()) || lower.startsWith(alias.trim() + ":")) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Converts text to a strict linear (single-column) structure.
     */
    private String linearize(String text) {
        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                result.append(trimmed).append("\n");
            } else {
                // Preserve single blank lines for section separation
                if (result.length() > 0 && result.charAt(result.length() - 1) != '\n') {
                    result.append("\n");
                }
            }
        }
        return result.toString().trim();
    }

    /**
     * Generates a clean ATS-compliant PDF using PDFBox.
     * Uses only standard fonts, no graphics, single column, proper margins.
     */
    private byte[] generatePDF(String text) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            float margin = 50;
            float fontSize = 11;
            float headerFontSize = 13;
            float leading = 14;
            float pageWidth = PDRectangle.LETTER.getWidth();
            float maxWidth = pageWidth - 2 * margin;
            float yStart = PDRectangle.LETTER.getHeight() - margin;

            String[] lines = text.split("\n");
            int lineIndex = 0;

            while (lineIndex < lines.length) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);

                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                    cs.beginText();
                    cs.setLeading(leading);
                    float y = yStart;
                    cs.newLineAtOffset(margin, y);

                    while (lineIndex < lines.length && y > margin + leading) {
                        String line = lines[lineIndex].trim();

                        if (line.isEmpty()) {
                            cs.newLine();
                            y -= leading;
                            lineIndex++;
                            continue;
                        }

                        // Check if this is a section header (ALL CAPS)
                        boolean isHeader = isSectionHeader(line);
                        PDType1Font currentFont = isHeader ? fontBold : fontRegular;
                        float currentSize = isHeader ? headerFontSize : fontSize;

                        cs.setFont(currentFont, currentSize);

                        // Word-wrap long lines
                        List<String> wrapped = wrapLine(line, currentFont, currentSize, maxWidth);
                        for (String wrappedLine : wrapped) {
                            if (y <= margin + leading) break;
                            cs.showText(wrappedLine);
                            cs.newLine();
                            y -= leading;
                        }
                        lineIndex++;
                    }

                    cs.endText();
                }
            }

            document.save(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Checks if a line is a normalized section header (ALL CAPS, known header).
     */
    private boolean isSectionHeader(String line) {
        return line.equals(line.toUpperCase())
                && line.length() <= 30
                && SECTION_HEADER_MAP.containsValue(line);
    }

    /**
     * Word-wraps a line to fit within the given width.
     */
    private List<String> wrapLine(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            String test = current.length() == 0 ? word : current + " " + word;
            float width = font.getStringWidth(test) / 1000 * fontSize;
            if (width > maxWidth && current.length() > 0) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(test);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }
}
