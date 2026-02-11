package com.kinovek.backend.service;

import com.kinovek.backend.config.ATSResumeConfig;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ATSPDFGenerator {

    private static final Logger log = LoggerFactory.getLogger(ATSPDFGenerator.class);

    @Autowired
    private ATSResumeConfig config;

    // Fonts (Helvetica - built-in PDF font, always available)
    private Font nameFont;
    private Font contactFont;
    private Font sectionHeadingFont;
    private Font jobTitleFont;
    private Font bodyFont;
    private Font companyFont;
    private Font dateFont;
    private Font skillCategoryFont;
    private Font skillValuesFont;
    private Font projectTitleFont;

    private void initFonts() {
        Color textColor = Color.BLACK;

        nameFont = new Font(Font.HELVETICA, config.getNameFontSize(), Font.BOLD, textColor);
        contactFont = new Font(Font.HELVETICA, config.getContactFontSize(), Font.NORMAL, textColor);
        sectionHeadingFont = new Font(Font.HELVETICA, config.getSectionHeadingSize(), Font.BOLD, textColor);
        jobTitleFont = new Font(Font.HELVETICA, config.getJobTitleSize(), Font.BOLD, textColor);
        bodyFont = new Font(Font.HELVETICA, config.getBodyTextSize(), Font.NORMAL, textColor);
        companyFont = new Font(Font.HELVETICA, config.getCompanyNameSize(), Font.NORMAL, textColor);
        dateFont = new Font(Font.HELVETICA, config.getDateSize(), Font.NORMAL, textColor);
        skillCategoryFont = new Font(Font.HELVETICA, config.getSkillCategorySize(), Font.BOLD, textColor);
        skillValuesFont = new Font(Font.HELVETICA, config.getSkillValuesSize(), Font.NORMAL, textColor);
        projectTitleFont = new Font(Font.HELVETICA, config.getProjectTitleSize(), Font.BOLD, textColor);
    }

    /**
     * Generate an ATS-optimized PDF resume.
     *
     * @param resumeData  Map containing all resume sections (personalInfo, summary, education, skills, experience, projects, certifications, achievements)
     * @param isFresher   true if candidate has less than 2 years experience
     * @return byte[] of the generated PDF
     */
    public byte[] generateATSResume(Map<String, Object> resumeData, boolean isFresher) {
        log.info("=== PDF GENERATOR: Starting | isFresher={} | sections={} ===", isFresher, resumeData.keySet());
        initFonts();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document(
                PageSize.A4,
                config.getMarginLeft(),
                config.getMarginRight(),
                config.getMarginTop(),
                config.getMarginBottom()
        );

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // ===== HEADER: Name + Contact + Line =====
            Map<String, Object> personalInfo = getMap(resumeData, "personalInfo");
            drawName(document, getString(personalInfo, "fullName"));
            drawContactInfo(document, personalInfo);
            drawHorizontalLine(document);

            // ===== SECTIONS IN ORDER =====
            List<String> sectionOrder;
            if (isFresher) {
                sectionOrder = Arrays.asList(
                        "summary", "education", "skills", "projects",
                        "experience", "certifications", "achievements"
                );
            } else {
                sectionOrder = Arrays.asList(
                        "summary", "skills", "experience", "projects",
                        "education", "certifications", "achievements"
                );
            }

            for (String section : sectionOrder) {
                switch (section) {
                    case "summary":
                        String summary = getString(resumeData, "summary");
                        if (summary != null && !summary.isBlank()) {
                            drawSummary(document, summary);
                        }
                        break;
                    case "education":
                        List<Map<String, Object>> education = getList(resumeData, "education");
                        if (education != null && !education.isEmpty()) {
                            drawEducation(document, education);
                        }
                        break;
                    case "skills":
                        Map<String, Object> skills = getMap(resumeData, "skills");
                        if (skills != null && !skills.isEmpty()) {
                            drawSkills(document, skills);
                        }
                        break;
                    case "experience":
                        List<Map<String, Object>> experience = getList(resumeData, "experience");
                        if (experience != null && !experience.isEmpty()) {
                            drawExperience(document, experience);
                        }
                        break;
                    case "projects":
                        List<Map<String, Object>> projects = getList(resumeData, "projects");
                        if (projects != null && !projects.isEmpty()) {
                            drawProjects(document, projects);
                        }
                        break;
                    case "certifications":
                        List<String> certifications = getStringList(resumeData, "certifications");
                        if (certifications != null && !certifications.isEmpty()) {
                            drawCertifications(document, certifications);
                        }
                        break;
                    case "achievements":
                        List<String> achievements = getStringList(resumeData, "achievements");
                        if (achievements != null && !achievements.isEmpty()) {
                            drawAchievements(document, achievements);
                        }
                        break;
                }
            }

            document.close();
            log.info("=== PDF GENERATOR: Document closed ===");
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate ATS resume PDF: " + e.getMessage(), e);
        }

        log.info("=== PDF GENERATOR: Complete | {} bytes ===", baos.size());
        return baos.toByteArray();
    }

    // ========== DRAWING METHODS ==========

    private void drawName(Document document, String name) throws DocumentException {
        if (name == null || name.isBlank()) return;

        Paragraph namePara = new Paragraph(name.trim(), nameFont);
        namePara.setAlignment(Element.ALIGN_CENTER);
        namePara.setSpacingAfter(config.getAfterName());
        document.add(namePara);
    }

    private void drawContactInfo(Document document, Map<String, Object> personalInfo) throws DocumentException {
        if (personalInfo == null) return;

        List<String> contactParts = new ArrayList<>();
        addIfPresent(contactParts, personalInfo, "email");
        addIfPresent(contactParts, personalInfo, "phone");
        addIfPresent(contactParts, personalInfo, "linkedin");
        addIfPresent(contactParts, personalInfo, "location");
        addIfPresent(contactParts, personalInfo, "github");
        addIfPresent(contactParts, personalInfo, "portfolio");

        if (contactParts.isEmpty()) return;

        String contactLine = String.join(" | ", contactParts);
        Paragraph contactPara = new Paragraph(contactLine, contactFont);
        contactPara.setAlignment(Element.ALIGN_CENTER);
        contactPara.setSpacingAfter(config.getAfterContact());
        document.add(contactPara);
    }

    private void drawHorizontalLine(Document document) throws DocumentException {
        Color lineColor = hexToColor(config.getLineColor());
        LineSeparator line = new LineSeparator(1f, 100f, lineColor, Element.ALIGN_CENTER, -2f);
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);
    }

    private void drawSectionHeading(Document document, String title) throws DocumentException {
        Paragraph heading = new Paragraph(title.toUpperCase(), sectionHeadingFont);
        heading.setSpacingBefore(config.getBeforeSectionHeading());
        heading.setSpacingAfter(2f);
        document.add(heading);

        // Thin gray line below heading
        Color lineColor = hexToColor(config.getLineColor());
        LineSeparator line = new LineSeparator(0.5f, 100f, lineColor, Element.ALIGN_CENTER, -2f);
        document.add(new Chunk(line));

        // Small space after the line
        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(config.getAfterSectionHeading());
        spacer.setLeading(1f);
        document.add(spacer);
    }

    private void drawSummary(Document document, String summary) throws DocumentException {
        drawSectionHeading(document, "PROFESSIONAL SUMMARY");

        Paragraph summaryPara = new Paragraph(summary.trim(), bodyFont);
        summaryPara.setLeading(config.getBodyTextSize() * config.getLineSpacing());
        summaryPara.setAlignment(Element.ALIGN_JUSTIFIED);
        summaryPara.setSpacingAfter(4f);
        document.add(summaryPara);
    }

    private void drawEducation(Document document, List<Map<String, Object>> educationList) throws DocumentException {
        drawSectionHeading(document, "EDUCATION");

        for (int i = 0; i < educationList.size(); i++) {
            Map<String, Object> edu = educationList.get(i);

            String degree = getString(edu, "degree");
            String institution = getString(edu, "institution");
            String year = getString(edu, "year");
            String score = getString(edu, "score");

            // Degree (bold) - right-aligned year
            if (degree != null) {
                if (year != null && !year.isBlank()) {
                    document.add(createLeftRightRow(degree, jobTitleFont, year, dateFont));
                } else {
                    Paragraph degreePara = new Paragraph(degree, jobTitleFont);
                    degreePara.setLeading(config.getBodyTextSize() * config.getLineSpacing());
                    document.add(degreePara);
                }
            }

            // Institution
            if (institution != null && !institution.isBlank()) {
                Paragraph instPara = new Paragraph(institution, companyFont);
                instPara.setLeading(config.getBodyTextSize() * config.getLineSpacing());
                document.add(instPara);
            }

            // Score (CGPA / Percentage)
            if (score != null && !score.isBlank()) {
                Paragraph scorePara = new Paragraph(score, bodyFont);
                scorePara.setLeading(config.getBodyTextSize() * config.getLineSpacing());
                document.add(scorePara);
            }

            // Space between education entries
            if (i < educationList.size() - 1) {
                Paragraph spacer = new Paragraph(" ");
                spacer.setSpacingAfter(config.getBetweenJobs());
                spacer.setLeading(1f);
                document.add(spacer);
            }
        }
    }

    private void drawSkills(Document document, Map<String, Object> skills) throws DocumentException {
        drawSectionHeading(document, "TECHNICAL SKILLS");

        for (Map.Entry<String, Object> entry : skills.entrySet()) {
            String category = entry.getKey();
            String values = String.valueOf(entry.getValue());

            Paragraph skillPara = new Paragraph();
            skillPara.setLeading(config.getSkillValuesSize() * config.getLineSpacing());

            Chunk categoryChunk = new Chunk(category + ": ", skillCategoryFont);
            Chunk valuesChunk = new Chunk(values, skillValuesFont);
            skillPara.add(categoryChunk);
            skillPara.add(valuesChunk);

            skillPara.setSpacingAfter(2f);
            document.add(skillPara);
        }
    }

    private void drawExperience(Document document, List<Map<String, Object>> experienceList) throws DocumentException {
        drawSectionHeading(document, "WORK EXPERIENCE");

        for (int i = 0; i < experienceList.size(); i++) {
            Map<String, Object> job = experienceList.get(i);

            String title = getString(job, "title");
            String company = getString(job, "company");
            String location = getString(job, "location");
            String dates = getString(job, "dates");

            // Job title (bold)
            if (title != null) {
                Paragraph titlePara = new Paragraph(title, jobTitleFont);
                titlePara.setLeading(config.getJobTitleSize() * config.getLineSpacing());
                document.add(titlePara);
            }

            // Company, Location — Dates (right-aligned)
            StringBuilder companyLine = new StringBuilder();
            if (company != null) companyLine.append(company);
            if (location != null && !location.isBlank()) {
                if (companyLine.length() > 0) companyLine.append(", ");
                companyLine.append(location);
            }

            if (dates != null && !dates.isBlank()) {
                document.add(createLeftRightRow(companyLine.toString(), companyFont, dates, dateFont));
            } else {
                Paragraph companyPara = new Paragraph(companyLine.toString(), companyFont);
                companyPara.setLeading(config.getCompanyNameSize() * config.getLineSpacing());
                document.add(companyPara);
            }

            // Bullet points
            List<String> bullets = getStringList(job, "bullets");
            if (bullets != null) {
                for (String bullet : bullets) {
                    drawBulletPoint(document, bullet);
                }
            }

            // Space between job entries
            if (i < experienceList.size() - 1) {
                Paragraph spacer = new Paragraph(" ");
                spacer.setSpacingAfter(config.getBetweenJobs());
                spacer.setLeading(1f);
                document.add(spacer);
            }
        }
    }

    private void drawProjects(Document document, List<Map<String, Object>> projectList) throws DocumentException {
        drawSectionHeading(document, "PROJECTS");

        for (int i = 0; i < projectList.size(); i++) {
            Map<String, Object> project = projectList.get(i);

            String name = getString(project, "name");
            String techStack = getString(project, "techStack");

            // Project name (bold) | Tech Stack
            Paragraph projectPara = new Paragraph();
            projectPara.setLeading(config.getProjectTitleSize() * config.getLineSpacing());

            if (name != null) {
                projectPara.add(new Chunk(name, projectTitleFont));
            }
            if (techStack != null && !techStack.isBlank()) {
                projectPara.add(new Chunk(" | ", bodyFont));
                projectPara.add(new Chunk(techStack, bodyFont));
            }
            document.add(projectPara);

            // Bullet points
            List<String> bullets = getStringList(project, "bullets");
            if (bullets != null) {
                for (String bullet : bullets) {
                    drawBulletPoint(document, bullet);
                }
            }

            // Space between project entries
            if (i < projectList.size() - 1) {
                Paragraph spacer = new Paragraph(" ");
                spacer.setSpacingAfter(config.getBetweenProjects());
                spacer.setLeading(1f);
                document.add(spacer);
            }
        }
    }

    private void drawCertifications(Document document, List<String> certifications) throws DocumentException {
        drawSectionHeading(document, "CERTIFICATIONS");

        for (String cert : certifications) {
            drawBulletPoint(document, cert);
        }
    }

    private void drawAchievements(Document document, List<String> achievements) throws DocumentException {
        drawSectionHeading(document, "ACHIEVEMENTS");

        for (String achievement : achievements) {
            drawBulletPoint(document, achievement);
        }
    }

    private void drawBulletPoint(Document document, String text) throws DocumentException {
        if (text == null || text.isBlank()) return;

        String bulletSymbol = config.getBulletSymbol();
        float indentation = config.getBulletIndentation();

        Paragraph bulletPara = new Paragraph();
        bulletPara.setLeading(config.getBodyTextSize() * config.getLineSpacing());
        bulletPara.setIndentationLeft(indentation);

        Chunk bulletChunk = new Chunk(bulletSymbol + "  ", bodyFont);
        Chunk textChunk = new Chunk(text.trim(), bodyFont);
        bulletPara.add(bulletChunk);
        bulletPara.add(textChunk);

        bulletPara.setSpacingAfter(config.getBetweenBullets());
        document.add(bulletPara);
    }

    // ========== UTILITY METHODS ==========

    /**
     * Creates a borderless single-row table with left-aligned text and right-aligned text.
     * Used for "Degree ... Year" and "Company, Location ... Dates" rows.
     */
    private PdfPTable createLeftRightRow(String leftText, Font leftFont, String rightText, Font rightFont) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{75f, 25f});

        PdfPCell leftCell = new PdfPCell(new Phrase(leftText, leftFont));
        leftCell.setBorder(PdfPCell.NO_BORDER);
        leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        leftCell.setPaddingLeft(0f);
        leftCell.setPaddingBottom(2f);

        PdfPCell rightCell = new PdfPCell(new Phrase(rightText, rightFont));
        rightCell.setBorder(PdfPCell.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.setPaddingRight(0f);
        rightCell.setPaddingBottom(2f);

        table.addCell(leftCell);
        table.addCell(rightCell);

        return table;
    }

    private void addIfPresent(List<String> parts, Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val != null && !String.valueOf(val).isBlank()) {
            parts.add(String.valueOf(val).trim());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> data, String key) {
        Object val = data.get(key);
        if (val instanceof Map) {
            return (Map<String, Object>) val;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getList(Map<String, Object> data, String key) {
        Object val = data.get(key);
        if (val instanceof List) {
            return (List<Map<String, Object>>) val;
        }
        return null;
    }

    private List<String> getStringList(Map<String, Object> data, String key) {
        Object val = data.get(key);
        if (val instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) val) {
                result.add(String.valueOf(item));
            }
            return result;
        }
        return null;
    }

    private String getString(Map<String, Object> data, String key) {
        Object val = data.get(key);
        return val != null ? String.valueOf(val) : null;
    }

    private Color hexToColor(String hex) {
        if (hex == null || hex.isBlank()) return Color.LIGHT_GRAY;
        hex = hex.replace("#", "");
        try {
            return new Color(
                    Integer.parseInt(hex.substring(0, 2), 16),
                    Integer.parseInt(hex.substring(2, 4), 16),
                    Integer.parseInt(hex.substring(4, 6), 16)
            );
        } catch (Exception e) {
            return Color.LIGHT_GRAY;
        }
    }
}
