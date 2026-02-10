package com.kinovek.backend.util;

import com.kinovek.backend.config.KeywordConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Matches keywords between a resume and a job description using the
 * skill categories and synonym mappings loaded from keywords.json.
 */
@Component
public class KeywordMatcher {

    private final KeywordConfig keywordConfig;

    // Common stop words to filter out when scanning JD text
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "is", "are", "was", "were", "be", "been",
            "being", "have", "has", "had", "do", "does", "did", "will", "would",
            "could", "should", "may", "might", "shall", "can", "need", "must",
            "we", "you", "he", "she", "it", "they", "i", "me", "my", "your",
            "our", "their", "this", "that", "these", "those", "not", "no",
            "if", "then", "than", "so", "as", "up", "out", "about", "into",
            "over", "after", "before", "between", "through", "during", "above",
            "below", "such", "each", "every", "all", "any", "both", "few",
            "more", "most", "other", "some", "only", "own", "same", "also",
            "just", "very", "well", "how", "what", "which", "who", "whom",
            "when", "where", "why", "able", "etc",
            "experience", "years", "year", "work", "working", "role", "team",
            "strong", "good", "excellent", "preferred", "required", "minimum",
            "plus", "including", "using", "knowledge", "understanding",
            "ability", "skills", "skill", "proficiency", "proficient",
            "familiar", "familiarity", "exposure",
            "looking", "seeking", "hiring", "join", "ideal", "candidate",
            "responsible", "responsibilities", "opportunity", "position",
            "company", "organization", "department", "apply", "application",
            "benefits", "salary", "compensation", "remote", "hybrid",
            "onsite", "full-time", "part-time", "contract", "description",
            "qualification", "qualifications", "requirement", "requirements",
            "deadline", "location", "based", "environment",
            "junior", "senior", "lead", "principal", "staff", "intern",
            "manager", "director", "associate", "analyst", "specialist",
            "engineer", "developer", "architect", "consultant", "coordinator",
            "officer", "executive", "administrator", "supervisor"
    );

    // Cache compiled word-boundary patterns
    private final Map<String, Pattern> boundaryPatternCache = new HashMap<>();

    @Autowired
    public KeywordMatcher(KeywordConfig keywordConfig) {
        this.keywordConfig = keywordConfig;
    }

    /**
     * Result of keyword matching between resume and job description.
     */
    public static class MatchResult {
        private final List<String> matchedKeywords;
        private final List<String> missingKeywords;
        private final double matchPercentage;

        public MatchResult(List<String> matchedKeywords, List<String> missingKeywords, double matchPercentage) {
            this.matchedKeywords = matchedKeywords;
            this.missingKeywords = missingKeywords;
            this.matchPercentage = matchPercentage;
        }

        public List<String> getMatchedKeywords() { return matchedKeywords; }
        public List<String> getMissingKeywords() { return missingKeywords; }
        public double getMatchPercentage() { return matchPercentage; }
    }

    /**
     * Matches keywords from the job description against the resume text.
     */
    public MatchResult match(String resumeText, String jobDescription) {
        // Step 1: Extract recognized keywords from the job description
        // Returns canonical → displayName mapping (canonical for dedup, display for output)
        Map<String, String> jdKeywordMap = extractKeywords(jobDescription);

        // Step 2: For each JD keyword, check resume (including synonym matching)
        String resumeLower = resumeText.toLowerCase();
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (Map.Entry<String, String> entry : jdKeywordMap.entrySet()) {
            String canonical = entry.getKey();
            String displayName = entry.getValue();

            if (isKeywordInResume(canonical, resumeLower)) {
                matched.add(displayName);
            } else {
                missing.add(displayName);
            }
        }

        // Step 3: Calculate match percentage
        double percentage = jdKeywordMap.isEmpty() ? 0 :
                Math.round((double) matched.size() / jdKeywordMap.size() * 100.0 * 10.0) / 10.0;

        return new MatchResult(matched, missing, percentage);
    }

    /**
     * Extracts recognized skills/keywords from text by checking each word/phrase
     * against the skillCategories lists and synonymMap from keywords.json.
     *
     * @return Map of canonical → displayName (canonical used for dedup, display for user-facing output)
     */
    private Map<String, String> extractKeywords(String text) {
        // canonical → displayName
        Map<String, String> keywords = new LinkedHashMap<>();
        // Track which original text fragments were matched by multi-word skills
        Set<String> consumedMultiWords = new HashSet<>();
        String textLower = text.toLowerCase();

        // ── Pass 1: Greedy multi-word matching (longest first) ──
        for (String multiWord : keywordConfig.getMultiWordSkills()) {
            if (textLower.contains(multiWord)) {
                String canonical = keywordConfig.getCanonical(multiWord);
                if (canonical != null && !keywords.containsKey(canonical)) {
                    String display = chooseDisplayName(canonical, multiWord);
                    keywords.put(canonical, display);
                    consumedMultiWords.add(multiWord);
                }
            }
        }

        // ── Pass 2: Single-word matching ──
        String[] words = text.split("[\\s,;|()\\[\\]{}]+");
        for (String word : words) {
            String cleaned = word.replaceAll("[^a-zA-Z0-9.#+\\-/]", "").trim();
            cleaned = cleaned.replaceAll("\\.$", "");

            if (cleaned.length() < 2 || STOP_WORDS.contains(cleaned.toLowerCase())) {
                continue;
            }

            if (keywordConfig.isKnownSkill(cleaned)) {
                String canonical = keywordConfig.getCanonical(cleaned);
                if (canonical != null && !keywords.containsKey(canonical)) {
                    // Check if this single word is already covered by a multi-word match
                    boolean alreadyCovered = false;
                    for (String mw : consumedMultiWords) {
                        if (mw.contains(cleaned.toLowerCase())) {
                            alreadyCovered = true;
                            break;
                        }
                    }
                    if (!alreadyCovered) {
                        String display = chooseDisplayName(canonical, cleaned);
                        keywords.put(canonical, display);
                    }
                }
            }
        }

        return keywords;
    }

    /**
     * Checks if a keyword (or any of its synonyms) appears in the resume text.
     * Uses word-boundary matching to prevent "Java" matching inside "JavaScript".
     */
    private boolean isKeywordInResume(String keyword, String resumeLower) {
        // Get all synonym forms for this keyword
        Set<String> allForms = keywordConfig.getAllForms(keyword);
        Set<String> formsToCheck = new HashSet<>(allForms);
        formsToCheck.add(keyword.toLowerCase());

        for (String form : formsToCheck) {
            if (containsWholeWord(resumeLower, form)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the text contains the target as a whole word/phrase,
     * not as a substring of a larger word.
     * e.g. "java" should NOT match inside "javascript"
     *      "c++" should match (special chars handled)
     *      "ci/cd" should match as-is
     */
    private boolean containsWholeWord(String text, String target) {
        if (!text.contains(target)) {
            return false;
        }

        // For multi-word phrases or phrases with special chars (ci/cd, .net, c++, c#)
        // use regex with appropriate boundaries
        Pattern pattern = boundaryPatternCache.computeIfAbsent(target, t -> {
            String escaped = Pattern.quote(t);
            // Use word boundary or start/end of string
            // \b doesn't work well with special chars, so use lookaround
            String regex = "(?<![a-zA-Z0-9])" + escaped + "(?![a-zA-Z0-9])";
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        });

        return pattern.matcher(text).find();
    }

    /**
     * Chooses the best display name for a keyword.
     * Prefers the shorter, more commonly-used form.
     * e.g., canonical "Amazon Web Services" with original "aws" → displays as "AWS"
     *        canonical "Continuous Deployment" with original "ci/cd" → displays as "CI/CD"
     *        canonical "Apache Kafka" with original "kafka" → displays as "Kafka"
     */
    private String chooseDisplayName(String canonical, String originalText) {
        // Get the properly-cased registered form of the original text
        // This bypasses synonym resolution, so "kafka" → "Kafka" not "Apache Kafka"
        String registeredForm = keywordConfig.getRegisteredForm(originalText);

        if (registeredForm != null && registeredForm.length() <= canonical.length()) {
            return registeredForm;
        }

        return canonical;
    }

}
