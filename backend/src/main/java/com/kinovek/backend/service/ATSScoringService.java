package com.kinovek.backend.service;

import com.kinovek.backend.dto.ATSScoreResponse;
import com.kinovek.backend.util.KeywordMatcher;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ATSScoringService {

    // Required resume sections
    private static final List<String> REQUIRED_SECTIONS = List.of(
            "Summary", "Education", "Experience", "Skills", "Certifications"
    );

    // Section header aliases for flexible detection
    private static final Map<String, List<String>> SECTION_ALIASES = Map.of(
            "Summary", List.of("summary", "objective", "profile", "about me", "professional summary", "career objective"),
            "Education", List.of("education", "academic", "qualification", "academics", "degree"),
            "Experience", List.of("experience", "work experience", "employment", "professional experience", "work history", "projects"),
            "Skills", List.of("skills", "technical skills", "core competencies", "technologies", "tools", "proficiencies"),
            "Certifications", List.of("certifications", "certificates", "licenses", "accreditations", "certified")
    );

    // Formatting red flags
    private static final List<String> FORMATTING_PENALTIES = List.of(
            "<table", "<img", "<image", "\\begin{tabular",
            "┌", "┐", "└", "┘", "│", "─",  // table border chars
            "\t\t\t"  // excessive tabs suggest columns
    );

    /**
     * Calculates the full ATS score for a resume against a job description.
     *
     * ATS Score = (Keyword Match Score x 0.4) + (Formatting Score x 0.3) + (Section Completeness Score x 0.3)
     *
     * @param resumeText     extracted text from resume
     * @param jobDescription job description text
     * @return ATSScoreResponse with overall score and section breakdown
     */
    public ATSScoreResponse calculateScore(String resumeText, String jobDescription) {
        // 1. Keyword Match Score
        KeywordMatcher.MatchResult matchResult = KeywordMatcher.match(resumeText, jobDescription);
        double keywordScore = matchResult.getMatchPercentage();

        // 2. Formatting Score
        double formattingScore = calculateFormattingScore(resumeText);
        String formattingFeedback = generateFormattingFeedback(resumeText, formattingScore);

        // 3. Section Completeness Score
        Map<String, Boolean> sectionPresence = detectSections(resumeText);
        double sectionScore = calculateSectionScore(sectionPresence);
        String sectionFeedback = generateSectionFeedback(sectionPresence);

        // Overall ATS Score
        double overallScore = (keywordScore * 0.4) + (formattingScore * 0.3) + (sectionScore * 0.3);
        int roundedScore = (int) Math.round(overallScore);
        roundedScore = Math.max(0, Math.min(100, roundedScore));

        // Build section analysis breakdown
        Map<String, Object> sectionAnalysis = new LinkedHashMap<>();

        Map<String, Object> skillsSection = new LinkedHashMap<>();
        skillsSection.put("score", (int) Math.round(keywordScore));
        skillsSection.put("feedback", matchResult.getMissingKeywords().isEmpty()
                ? "Great keyword alignment with the job description"
                : "Missing " + matchResult.getMissingKeywords().size() + " key skills");
        sectionAnalysis.put("skills", skillsSection);

        Map<String, Object> experienceSection = new LinkedHashMap<>();
        boolean hasExperience = sectionPresence.getOrDefault("Experience", false);
        experienceSection.put("score", hasExperience ? (int) Math.round(keywordScore * 0.9) : 30);
        experienceSection.put("feedback", hasExperience ? "Experience section found" : "Experience section missing");
        sectionAnalysis.put("experience", experienceSection);

        Map<String, Object> formattingSection = new LinkedHashMap<>();
        formattingSection.put("score", (int) Math.round(formattingScore));
        formattingSection.put("feedback", formattingFeedback);
        sectionAnalysis.put("formatting", formattingSection);

        Map<String, Object> completenessSection = new LinkedHashMap<>();
        completenessSection.put("score", (int) Math.round(sectionScore));
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

    /**
     * Scores formatting from 0-100. Penalizes tables, images, columns.
     */
    private double calculateFormattingScore(String resumeText) {
        double score = 100.0;
        String lower = resumeText.toLowerCase();

        for (String penalty : FORMATTING_PENALTIES) {
            if (lower.contains(penalty.toLowerCase())) {
                score -= 15;
            }
        }

        // Penalize very short resumes
        if (resumeText.length() < 200) {
            score -= 10;
        }

        // Penalize excessive special characters (likely decorative)
        long specialChars = resumeText.chars()
                .filter(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c)
                        && c != '.' && c != ',' && c != '-' && c != ':' && c != '(' && c != ')' && c != '/')
                .count();
        if (specialChars > resumeText.length() * 0.1) {
            score -= 10;
        }

        return Math.max(0, Math.min(100, score));
    }

    /**
     * Detects which required sections are present in the resume.
     */
    private Map<String, Boolean> detectSections(String resumeText) {
        String lower = resumeText.toLowerCase();
        Map<String, Boolean> result = new LinkedHashMap<>();

        for (String section : REQUIRED_SECTIONS) {
            List<String> aliases = SECTION_ALIASES.get(section);
            boolean found = aliases.stream().anyMatch(lower::contains);
            result.put(section, found);
        }

        return result;
    }

    /**
     * Calculates section completeness score (0-100).
     */
    private double calculateSectionScore(Map<String, Boolean> sectionPresence) {
        long found = sectionPresence.values().stream().filter(v -> v).count();
        return (double) found / sectionPresence.size() * 100.0;
    }

    private String generateFormattingFeedback(String resumeText, double score) {
        if (score >= 90) return "Clean, ATS-friendly formatting";
        if (score >= 70) return "Minor formatting issues detected";
        if (score >= 50) return "Formatting needs improvement - possible tables or images detected";
        return "Significant formatting issues - remove tables, images, and complex layouts";
    }

    private String generateSectionFeedback(Map<String, Boolean> sectionPresence) {
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : sectionPresence.entrySet()) {
            if (!entry.getValue()) {
                missing.add(entry.getKey());
            }
        }
        if (missing.isEmpty()) return "All required sections present";
        return "Missing sections: " + String.join(", ", missing);
    }
}
