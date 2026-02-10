package com.kinovek.backend.service;

import com.kinovek.backend.config.KeywordConfig;
import com.kinovek.backend.dto.ATSScoreResponse;
import com.kinovek.backend.util.KeywordMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ATSScoringService {

    private final KeywordMatcher keywordMatcher;
    private final KeywordConfig keywordConfig;

    // Regex patterns for contact info detection
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(\\+?\\d{1,3}[\\s\\-]?)?(\\(?\\d{2,4}\\)?[\\s\\-]?)?\\d{3,5}[\\s\\-]?\\d{3,5}");

    // Column/table special characters that indicate non-ATS-friendly formatting
    private static final String COLUMN_CHARS = "║│┃─┌┐└┘├┤┬┴┼╔╗╚╝╠╣╦╩╬";

    @Autowired
    public ATSScoringService(KeywordMatcher keywordMatcher, KeywordConfig keywordConfig) {
        this.keywordMatcher = keywordMatcher;
        this.keywordConfig = keywordConfig;
    }

    /**
     * Calculates the full ATS score for a resume against a job description.
     *
     * ATS Score = (Keyword Match Score × 0.50) + (Formatting Score × 0.25) + (Section Completeness Score × 0.25)
     *
     * @param resumeText     extracted text from resume
     * @param jobDescription job description text
     * @return ATSScoreResponse with overall score and section breakdown
     */
    public ATSScoreResponse calculateScore(String resumeText, String jobDescription) {
        // 1. Keyword Match Score (0–100)
        KeywordMatcher.MatchResult matchResult = keywordMatcher.match(resumeText, jobDescription);
        double keywordScore = matchResult.getMatchPercentage();

        // 2. Formatting Score (0–100)
        double formattingScore = calculateFormattingScore(resumeText);
        List<String> formattingIssues = detectFormattingIssues(resumeText);
        String formattingFeedback = generateFormattingFeedback(formattingIssues, formattingScore);

        // 3. Section Completeness Score (0–100)
        Map<String, Boolean> sectionPresence = detectSections(resumeText);
        double sectionScore = calculateSectionScore(resumeText, sectionPresence);
        String sectionFeedback = generateSectionFeedback(sectionPresence, resumeText);

        // Overall ATS Score = weighted sum, clamped 0–100
        double overallScore = (keywordScore * 0.50) + (formattingScore * 0.25) + (sectionScore * 0.25);
        int roundedScore = (int) Math.round(overallScore);
        roundedScore = Math.max(0, Math.min(100, roundedScore));

        // ── Build section analysis breakdown ──
        Map<String, Object> sectionAnalysis = new LinkedHashMap<>();

        // Skills / Keyword analysis
        Map<String, Object> skillsSection = new LinkedHashMap<>();
        skillsSection.put("score", (int) Math.round(keywordScore));
        skillsSection.put("matched", matchResult.getMatchedKeywords());
        skillsSection.put("missing", matchResult.getMissingKeywords());
        skillsSection.put("feedback", matchResult.getMissingKeywords().isEmpty()
                ? "Great keyword alignment with the job description"
                : "Missing " + matchResult.getMissingKeywords().size() + " key skill(s) from the job description");
        sectionAnalysis.put("skills", skillsSection);

        // Experience section
        Map<String, Object> experienceSection = new LinkedHashMap<>();
        boolean hasExperience = sectionPresence.getOrDefault("Experience", false);
        experienceSection.put("score", hasExperience ? Math.max(60, (int) Math.round(keywordScore * 0.9)) : 20);
        experienceSection.put("feedback", hasExperience
                ? "Experience section detected"
                : "Experience section missing — this is critical for ATS");
        sectionAnalysis.put("experience", experienceSection);

        // Formatting analysis
        Map<String, Object> formattingSection = new LinkedHashMap<>();
        formattingSection.put("score", (int) Math.round(formattingScore));
        formattingSection.put("issues", formattingIssues);
        formattingSection.put("feedback", formattingFeedback);
        sectionAnalysis.put("formatting", formattingSection);

        // Section completeness analysis
        Map<String, Object> completenessSection = new LinkedHashMap<>();
        completenessSection.put("score", (int) Math.round(sectionScore));
        completenessSection.put("sections", sectionPresence);
        completenessSection.put("feedback", sectionFeedback);
        sectionAnalysis.put("sectionCompleteness", completenessSection);

        // Build response
        ATSScoreResponse response = new ATSScoreResponse();
        response.setSuccess(true);
        response.setOverallScore(roundedScore);
        response.setKeywordMatchScore(Math.round(keywordScore * 10.0) / 10.0);
        response.setFormattingScore(Math.round(formattingScore * 10.0) / 10.0);
        response.setSectionCompletenessScore(Math.round(sectionScore * 10.0) / 10.0);
        response.setSectionBreakdown(sectionAnalysis);

        return response;
    }

    // ────────────────────────────────────────────────────────────────
    //  FORMATTING SCORE  (start at 100, subtract penalties, min 0)
    // ────────────────────────────────────────────────────────────────

    /**
     * Scores resume formatting from 0–100.
     * Penalties:
     *   - Tables detected:           −20
     *   - Column/box-drawing chars:  −15
     *   - Image references:          −15
     *   - Very short (< 100 words):  −20
     */
    private double calculateFormattingScore(String resumeText) {
        double score = 100.0;
        String lower = resumeText.toLowerCase();

        // Tables
        if (lower.contains("<table") || lower.contains("\\begin{tabular") || lower.contains("\t\t\t")) {
            score -= 20;
        }

        // Column / box-drawing special characters
        boolean hasColumnChars = false;
        for (char c : COLUMN_CHARS.toCharArray()) {
            if (resumeText.indexOf(c) >= 0) {
                hasColumnChars = true;
                break;
            }
        }
        if (hasColumnChars) {
            score -= 15;
        }

        // Image references
        if (lower.contains("<img") || lower.contains("<image") || lower.contains("[image")
                || lower.contains(".png") || lower.contains(".jpg") || lower.contains(".jpeg")) {
            score -= 15;
        }

        // Very short resume (under 100 words)
        int wordCount = resumeText.split("\\s+").length;
        if (wordCount < 100) {
            score -= 20;
        }

        return Math.max(0, Math.min(100, score));
    }

    /** Returns a list of detected formatting issues (used in the breakdown). */
    private List<String> detectFormattingIssues(String resumeText) {
        List<String> issues = new ArrayList<>();
        String lower = resumeText.toLowerCase();

        if (lower.contains("<table") || lower.contains("\\begin{tabular") || lower.contains("\t\t\t")) {
            issues.add("Table-based layout detected — most ATS parsers cannot read tables");
        }

        boolean hasColumnChars = false;
        for (char c : COLUMN_CHARS.toCharArray()) {
            if (resumeText.indexOf(c) >= 0) { hasColumnChars = true; break; }
        }
        if (hasColumnChars) {
            issues.add("Column or box-drawing characters found — indicates multi-column layout");
        }

        if (lower.contains("<img") || lower.contains("<image") || lower.contains("[image")
                || lower.contains(".png") || lower.contains(".jpg") || lower.contains(".jpeg")) {
            issues.add("Image references found — ATS cannot parse images");
        }

        int wordCount = resumeText.split("\\s+").length;
        if (wordCount < 100) {
            issues.add("Resume is very short (" + wordCount + " words) — aim for at least 200–400 words");
        }

        return issues;
    }

    // ────────────────────────────────────────────────────────────────
    //  SECTION COMPLETENESS SCORE  (point-based, max 100)
    // ────────────────────────────────────────────────────────────────
    //  Contact Info (name/email/phone):  15 pts
    //  Summary / Objective:              15 pts
    //  Experience:                       25 pts
    //  Education:                        20 pts
    //  Skills:                           20 pts
    //  Certifications OR Projects:        5 bonus pts
    //  Total possible:                  100 pts

    /**
     * Detects which sections are present in the resume
     * using sectionHeaders from keywords.json.
     * Uses LINE-BASED matching: a section header must appear at the beginning
     * of a line (with optional whitespace), not just anywhere in the text.
     * This prevents "Java and Python projects" from matching as a "Projects" section.
     */
    private Map<String, Boolean> detectSections(String resumeText) {
        String lower = resumeText.toLowerCase();
        String[] lines = lower.split("\\r?\\n");
        Map<String, Boolean> result = new LinkedHashMap<>();
        Map<String, List<String>> allHeaders = keywordConfig.getSectionHeaders();

        String[] sectionsToDetect = {
                "contact", "summary", "experience", "education", "skills",
                "certifications", "projects"
        };

        for (String section : sectionsToDetect) {
            List<String> aliases = allHeaders.get(section);
            boolean found = false;

            if (aliases != null) {
                // Check each line to see if it starts with or is a section header
                for (String line : lines) {
                    String trimmedLine = line.trim();
                    if (trimmedLine.isEmpty()) continue;

                    for (String alias : aliases) {
                        String aliasLower = alias.toLowerCase();
                        // Line IS the header (exact or with trailing colon/dash)
                        if (trimmedLine.equals(aliasLower)
                                || trimmedLine.equals(aliasLower + ":")
                                || trimmedLine.equals(aliasLower + " :")) {
                            found = true;
                            break;
                        }
                        // Line STARTS WITH the header followed by whitespace/punctuation/EOL
                        // This catches "SKILLS & COMPETENCIES" or "Experience:" etc.
                        if (trimmedLine.startsWith(aliasLower)
                                && trimmedLine.length() > aliasLower.length()) {
                            char nextChar = trimmedLine.charAt(aliasLower.length());
                            if (!Character.isLetterOrDigit(nextChar)) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (found) break;
                }
            }

            // Special case: Contact — also detect via email/phone regex
            if ("contact".equals(section) && !found) {
                boolean hasEmail = EMAIL_PATTERN.matcher(resumeText).find();
                boolean hasPhone = PHONE_PATTERN.matcher(resumeText).find();
                if (hasEmail || hasPhone) {
                    found = true;
                }
            }

            String displayName = section.substring(0, 1).toUpperCase() + section.substring(1);
            result.put(displayName, found);
        }

        return result;
    }

    /**
     * Calculates section completeness score using a point-based system.
     *
     * Contact Info:     15 pts  (detected by email/phone regex, not just header)
     * Summary:          15 pts
     * Experience:       25 pts
     * Education:        20 pts
     * Skills:           20 pts
     * Certs/Projects:    5 pts  (bonus)
     */
    private double calculateSectionScore(String resumeText, Map<String, Boolean> sectionPresence) {
        double score = 0;

        // Contact Info — 15 pts
        // Check for actual email/phone presence, not just a "Contact" header
        boolean hasContact = sectionPresence.getOrDefault("Contact", false);
        boolean hasEmail = EMAIL_PATTERN.matcher(resumeText).find();
        boolean hasPhone = PHONE_PATTERN.matcher(resumeText).find();
        if (hasContact || hasEmail || hasPhone) {
            // Award partial credit: header=5, email=5, phone=5
            if (hasContact) score += 5;
            if (hasEmail) score += 5;
            if (hasPhone) score += 5;
        }

        // Summary / Objective — 15 pts
        if (sectionPresence.getOrDefault("Summary", false)) {
            score += 15;
        }

        // Experience — 25 pts (heaviest because it's the most important section)
        if (sectionPresence.getOrDefault("Experience", false)) {
            score += 25;
        }

        // Education — 20 pts
        if (sectionPresence.getOrDefault("Education", false)) {
            score += 20;
        }

        // Skills — 20 pts
        if (sectionPresence.getOrDefault("Skills", false)) {
            score += 20;
        }

        // Certifications OR Projects — 5 bonus pts
        boolean hasCerts = sectionPresence.getOrDefault("Certifications", false);
        boolean hasProjects = sectionPresence.getOrDefault("Projects", false);
        if (hasCerts || hasProjects) {
            score += 5;
        }

        return Math.max(0, Math.min(100, score));
    }

    // ────────────────────────────────────────────────────────────────
    //  FEEDBACK GENERATORS
    // ────────────────────────────────────────────────────────────────

    private String generateFormattingFeedback(List<String> issues, double score) {
        if (issues.isEmpty()) return "Clean, ATS-friendly formatting";
        if (score >= 80) return "Minor formatting concerns detected";
        if (score >= 50) return "Formatting needs improvement — " + issues.size() + " issue(s) found";
        return "Significant formatting problems — remove tables, images, and complex layouts";
    }

    private String generateSectionFeedback(Map<String, Boolean> sectionPresence, String resumeText) {
        List<String> missing = new ArrayList<>();

        // Check contact via regex too, not just header
        boolean hasEmail = EMAIL_PATTERN.matcher(resumeText).find();
        boolean hasPhone = PHONE_PATTERN.matcher(resumeText).find();
        boolean contactOk = sectionPresence.getOrDefault("Contact", false) || hasEmail || hasPhone;
        if (!contactOk) missing.add("Contact Info");

        if (!sectionPresence.getOrDefault("Summary", false)) missing.add("Summary/Objective");
        if (!sectionPresence.getOrDefault("Experience", false)) missing.add("Experience");
        if (!sectionPresence.getOrDefault("Education", false)) missing.add("Education");
        if (!sectionPresence.getOrDefault("Skills", false)) missing.add("Skills");

        if (missing.isEmpty()) return "All essential sections present";
        return "Missing sections: " + String.join(", ", missing);
    }
}
