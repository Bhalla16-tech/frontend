package com.kinovek.backend.service;

import com.kinovek.backend.config.KeywordConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes original parsed resume data + keyword analysis results and produces
 * an enhanced resume Map suitable for ATSPDFGenerator.
 *
 * Enhancements include: adding missing keywords to skills, fixing bullet points,
 * cleaning banned phrases from summary, removing Indian-specific items,
 * and suggesting certifications.
 */
@Service
public class ResumeRewriter {

    private static final Logger log = LoggerFactory.getLogger(ResumeRewriter.class);

    @Autowired
    private KeywordConfig keywordConfig;

    @Autowired
    private ATSContentService atsContentService;

    private static final int MAX_SUMMARY_WORDS = 60;
    private static final int MAX_BULLET_LENGTH = 120;

    private static final Pattern YEAR_RANGE_PATTERN =
            Pattern.compile("(\\d{4})\\s*[-–—]\\s*(\\d{4}|[Pp]resent|[Cc]urrent|[Oo]ngoing)");

    // Indian-specific personal fields to remove
    private static final Set<String> FIELDS_TO_REMOVE = new HashSet<>(Arrays.asList(
            "dateOfBirth", "dob", "date_of_birth", "gender", "sex",
            "maritalStatus", "marital_status", "fatherName", "father_name",
            "fathersName", "nationality", "passportNumber", "passport_number",
            "passport", "photo", "photograph", "declaration"
    ));

    /**
     * Enhance the original resume data using analysis results and job description.
     *
     * @param originalResumeData parsed resume from ResumeTextParserService
     * @param analysisResults    must contain "matchedKeywords" and "missingKeywords" as List<String>
     * @param jobDescription     the target job description text
     * @return enhanced resume Map ready for ATSPDFGenerator, with "isFresher" flag
     */
    public Map<String, Object> enhanceResume(
            Map<String, Object> originalResumeData,
            Map<String, Object> analysisResults,
            String jobDescription) {

        log.info("=== REWRITER: Enhancing resume ===");
        log.info("Original data keys: {} | JD length: {}", originalResumeData.keySet(), jobDescription.length());

        // Deep copy to avoid mutating the original
        Map<String, Object> enhanced = deepCopy(originalResumeData);

        try {
            // 1. Detect industry
            String industry = atsContentService.detectIndustry(jobDescription);
            log.info("=== REWRITER STEP 1: Industry detected: {} ===", industry);

            // 7. Determine fresher vs experienced (need this early for other decisions)
            boolean isFresher = determineIsFresher(enhanced);
            enhanced.put("isFresher", isFresher);
            log.info("=== REWRITER STEP 2: isFresher={} ===", isFresher);

            // 2. Add missing keywords to skills
            List<String> missingKeywords = getStringList(analysisResults, "missingKeywords");
            List<String> matchedKeywords = getStringList(analysisResults, "matchedKeywords");
            log.info("=== REWRITER STEP 3: Missing keywords: {} | Matched: {} ===", missingKeywords.size(), matchedKeywords.size());
            addMissingKeywordsToSkills(enhanced, missingKeywords);
            log.info("Missing keywords added to skills");

            // 3. Enhance professional summary
            enhanceSummary(enhanced, industry, isFresher, matchedKeywords, jobDescription);
            log.info("=== REWRITER STEP 4: Summary enhanced ===");

            // 4. Fix bullet points
            fixBulletPoints(enhanced, industry);
            log.info("=== REWRITER STEP 5: Bullet points fixed ===");

            // 5. Remove Indian-specific items
            removeIndianSpecificItems(enhanced);
            log.info("=== REWRITER STEP 6: Indian-specific items removed ===");

            // 6. Ensure all sections have content
            ensureSectionContent(enhanced, isFresher, matchedKeywords);
            log.info("=== REWRITER STEP 7: Section content ensured ===");

            // 8. Suggest certifications
            suggestCertifications(enhanced, industry);
            log.info("=== REWRITER STEP 8: Certifications suggested ===");
            log.info("=== REWRITER: Enhancement complete ===");

        } catch (Exception e) {
            System.err.println("⚠️ Resume enhancement encountered an error: " + e.getMessage());
            // Still return whatever we have — fault tolerant
            if (!enhanced.containsKey("isFresher")) {
                enhanced.put("isFresher", true);
            }
        }

        return enhanced;
    }

    // ===== 2. ADD MISSING KEYWORDS TO SKILLS =====

    @SuppressWarnings("unchecked")
    private void addMissingKeywordsToSkills(Map<String, Object> enhanced, List<String> missingKeywords) {
        if (missingKeywords == null || missingKeywords.isEmpty()) return;

        Object skillsObj = enhanced.get("skills");
        Map<String, String> skills;
        if (skillsObj instanceof Map) {
            skills = new LinkedHashMap<>((Map<String, String>) skillsObj);
        } else {
            skills = new LinkedHashMap<>();
        }

        for (String keyword : missingKeywords) {
            // Skip soft skills and vague terms
            if (isSoftSkill(keyword)) continue;

            String displayForm = keywordConfig.getRegisteredForm(keyword);
            if (displayForm == null) displayForm = keyword;

            // Find which category this skill belongs to
            String category = findSkillCategory(skills, displayForm);

            // Add to appropriate category
            String existing = skills.getOrDefault(category, "");
            if (!existing.toLowerCase().contains(displayForm.toLowerCase())) {
                if (existing.isEmpty()) {
                    skills.put(category, displayForm);
                } else {
                    skills.put(category, existing + ", " + displayForm);
                }
            }
        }

        enhanced.put("skills", skills);
    }

    /**
     * Try to find the best category for a skill based on existing resume categories.
     * Falls back to "Additional Skills".
     */
    private String findSkillCategory(Map<String, String> existingSkills, String skill) {
        String skillLower = skill.toLowerCase();

        // Common category keyword mapping
        Map<String, List<String>> categoryHints = new LinkedHashMap<>();
        categoryHints.put("Programming Languages", Arrays.asList("java", "python", "javascript", "c++", "c#", "go", "rust", "ruby", "php", "swift", "kotlin", "typescript", "scala", "r", "perl", "dart"));
        categoryHints.put("Frameworks", Arrays.asList("spring", "react", "angular", "vue", "django", "flask", "express", "node", ".net", "laravel", "rails", "next.js", "nuxt"));
        categoryHints.put("Databases", Arrays.asList("mysql", "postgresql", "mongodb", "redis", "oracle", "sql server", "cassandra", "dynamodb", "elasticsearch", "sqlite"));
        categoryHints.put("Cloud & DevOps", Arrays.asList("aws", "azure", "gcp", "docker", "kubernetes", "jenkins", "terraform", "ansible", "ci/cd", "devops"));
        categoryHints.put("Tools", Arrays.asList("git", "jira", "confluence", "slack", "vs code", "intellij", "postman", "swagger", "maven", "gradle"));

        // First, check if skill matches any hint category
        for (Map.Entry<String, List<String>> entry : categoryHints.entrySet()) {
            for (String hint : entry.getValue()) {
                if (skillLower.contains(hint) || hint.contains(skillLower)) {
                    // Check if this category (or similar) exists in existing skills
                    String matchedCategory = findMatchingExistingCategory(existingSkills, entry.getKey());
                    if (matchedCategory != null) return matchedCategory;
                }
            }
        }

        // Second, try to match to an existing category by checking if similar skills are there
        for (Map.Entry<String, String> entry : existingSkills.entrySet()) {
            // If the category values contain related skills, add here
            String values = entry.getValue().toLowerCase();
            if (values.contains(skillLower) || skillLower.contains(values)) {
                return entry.getKey();
            }
        }

        return "Additional Skills";
    }

    private String findMatchingExistingCategory(Map<String, String> existingSkills, String targetCategory) {
        String targetLower = targetCategory.toLowerCase();
        for (String category : existingSkills.keySet()) {
            String catLower = category.toLowerCase();
            if (catLower.contains(targetLower) || targetLower.contains(catLower) ||
                    similarCategory(catLower, targetLower)) {
                return category;
            }
        }
        return null;
    }

    private boolean similarCategory(String cat1, String cat2) {
        // "programming languages" ~ "languages", "frameworks" ~ "frameworks & libraries"
        Set<String> words1 = new HashSet<>(Arrays.asList(cat1.split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(cat2.split("\\s+")));
        words1.retainAll(words2);
        return !words1.isEmpty();
    }

    private boolean isSoftSkill(String keyword) {
        String lower = keyword.toLowerCase();
        Set<String> softSkills = new HashSet<>(Arrays.asList(
                "communication", "leadership", "teamwork", "problem solving",
                "time management", "critical thinking", "adaptability", "creativity",
                "collaboration", "interpersonal", "decision making", "organization",
                "work ethic", "attention to detail", "self-motivated", "flexibility",
                "presentation", "negotiation", "conflict resolution", "multitasking"
        ));
        return softSkills.contains(lower);
    }

    // ===== 3. ENHANCE PROFESSIONAL SUMMARY =====

    private void enhanceSummary(Map<String, Object> enhanced, String industry,
                                boolean isFresher, List<String> matchedKeywords, String jobDescription) {
        String summary = getString(enhanced, "summary");

        if (summary != null && !summary.isBlank()) {
            // Clean banned phrases from existing summary
            List<String> bannedPhrases = atsContentService.getBannedSummaryPhrases();
            for (String banned : bannedPhrases) {
                summary = summary.replaceAll("(?i)" + Pattern.quote(banned), "").trim();
            }
            // Clean up double spaces
            summary = summary.replaceAll("\\s{2,}", " ").trim();
            // Truncate if too long
            summary = truncateToWords(summary, MAX_SUMMARY_WORDS);
            enhanced.put("summary", summary);
        } else {
            // Generate summary from template
            List<String> templates = atsContentService.getSummaryTemplates(industry, isFresher);
            if (!templates.isEmpty()) {
                String template = templates.get(0); // Use first template
                summary = fillSummaryTemplate(template, enhanced, matchedKeywords, jobDescription);
                enhanced.put("summary", summary);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String fillSummaryTemplate(String template, Map<String, Object> resumeData,
                                        List<String> matchedKeywords, String jobDescription) {
        String result = template;

        // Extract target role from JD (first few words or title)
        String targetRole = extractRoleFromJD(jobDescription);
        result = result.replace("{targetRole}", targetRole);
        result = result.replace("{target_role}", targetRole);

        // Fill skill placeholders
        if (matchedKeywords != null && !matchedKeywords.isEmpty()) {
            for (int i = 0; i < Math.min(matchedKeywords.size(), 5); i++) {
                result = result.replace("{skill" + (i + 1) + "}", matchedKeywords.get(i));
            }
        }
        // Clean unfilled placeholders
        result = result.replaceAll("\\{skill\\d+}", "relevant technologies");

        // Fill degree
        Map<String, Object> personalInfo = getMap(resumeData, "personalInfo");
        String fullName = getString(personalInfo, "fullName");
        result = result.replace("{name}", fullName != null ? fullName : "");

        // Try to get degree from education
        Object eduObj = resumeData.get("education");
        if (eduObj instanceof List && !((List<?>) eduObj).isEmpty()) {
            Object firstEdu = ((List<?>) eduObj).get(0);
            if (firstEdu instanceof Map) {
                String degree = getString((Map<String, Object>) firstEdu, "degree");
                result = result.replace("{degree}", degree != null ? degree : "relevant degree");
            }
        }
        result = result.replace("{degree}", "relevant degree");

        // Fill years of experience
        int years = estimateYearsOfExperience(resumeData);
        result = result.replace("{years}", String.valueOf(years));
        result = result.replace("{experience_years}", String.valueOf(years));

        // Clean any remaining unfilled placeholders
        result = result.replaceAll("\\{[^}]+}", "");
        result = result.replaceAll("\\s{2,}", " ").trim();

        return truncateToWords(result, MAX_SUMMARY_WORDS);
    }

    private String extractRoleFromJD(String jobDescription) {
        if (jobDescription == null || jobDescription.isBlank()) return "Software Professional";

        // Common role patterns
        String[] rolePatterns = {
                "(?i)(senior|junior|lead|principal|staff)?\\s*(software|full[- ]?stack|frontend|backend|data|devops|cloud|mobile|web)\\s*(developer|engineer|architect|analyst|scientist)",
                "(?i)(java|python|react|angular|node\\.?js|.net)\\s*(developer|engineer)",
                "(?i)(project|product|program|engineering)\\s*manager",
                "(?i)(ui/?ux|graphic|visual)\\s*designer"
        };

        for (String pattern : rolePatterns) {
            Matcher matcher = Pattern.compile(pattern).matcher(jobDescription);
            if (matcher.find()) {
                return capitalizeWords(matcher.group().trim());
            }
        }

        // Fallback: take first line if short
        String firstLine = jobDescription.split("\\r?\\n")[0].trim();
        if (firstLine.length() <= 50) return firstLine;

        return "Software Professional";
    }

    // ===== 4. FIX BULLET POINTS =====

    @SuppressWarnings("unchecked")
    private void fixBulletPoints(Map<String, Object> enhanced, String industry) {
        List<String> bannedStarters = atsContentService.getBannedBulletStarters();
        List<String> actionVerbs = atsContentService.getAllActionVerbs(industry);
        int verbIndex = 0;

        // Fix experience bullets
        Object expObj = enhanced.get("experience");
        if (expObj instanceof List) {
            List<Map<String, Object>> experience = (List<Map<String, Object>>) expObj;
            for (Map<String, Object> job : experience) {
                Object bulletsObj = job.get("bullets");
                if (bulletsObj instanceof List) {
                    List<String> bullets = (List<String>) bulletsObj;
                    List<String> fixedBullets = new ArrayList<>();
                    for (String bullet : bullets) {
                        String fixed = fixBullet(bullet, bannedStarters, actionVerbs, verbIndex);
                        fixedBullets.add(fixed);
                        verbIndex = (verbIndex + 1) % Math.max(1, actionVerbs.size());
                    }
                    job.put("bullets", fixedBullets);
                }
            }
        }

        // Fix project bullets
        Object projObj = enhanced.get("projects");
        if (projObj instanceof List) {
            List<Map<String, Object>> projects = (List<Map<String, Object>>) projObj;
            for (Map<String, Object> project : projects) {
                Object bulletsObj = project.get("bullets");
                if (bulletsObj instanceof List) {
                    List<String> bullets = (List<String>) bulletsObj;
                    List<String> fixedBullets = new ArrayList<>();
                    for (String bullet : bullets) {
                        String fixed = fixBullet(bullet, bannedStarters, actionVerbs, verbIndex);
                        fixedBullets.add(fixed);
                        verbIndex = (verbIndex + 1) % Math.max(1, actionVerbs.size());
                    }
                    project.put("bullets", fixedBullets);
                }
            }
        }
    }

    private String fixBullet(String bullet, List<String> bannedStarters,
                              List<String> actionVerbs, int verbIndex) {
        if (bullet == null || bullet.isBlank()) return bullet;

        String trimmed = bullet.trim();

        // Check for banned starters and replace
        String lower = trimmed.toLowerCase();
        for (String banned : bannedStarters) {
            if (lower.startsWith(banned)) {
                trimmed = trimmed.substring(banned.length()).trim();
                // Remove any leading punctuation left over
                trimmed = trimmed.replaceAll("^[,;:\\s]+", "").trim();
                break;
            }
        }

        // Ensure bullet starts with an action verb
        if (!actionVerbs.isEmpty() && !startsWithVerb(trimmed, actionVerbs)) {
            String verb = actionVerbs.get(verbIndex % actionVerbs.size());
            // Capitalize first letter of remaining text after verb
            if (!trimmed.isEmpty()) {
                trimmed = verb + " " + Character.toLowerCase(trimmed.charAt(0)) + trimmed.substring(1);
            }
        }

        // Capitalize first letter
        if (!trimmed.isEmpty()) {
            trimmed = Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
        }

        // Truncate if too long
        if (trimmed.length() > MAX_BULLET_LENGTH) {
            trimmed = trimmed.substring(0, MAX_BULLET_LENGTH - 3).trim() + "...";
        }

        return trimmed;
    }

    private boolean startsWithVerb(String text, List<String> actionVerbs) {
        String firstWord = text.split("\\s+")[0].toLowerCase();
        for (String verb : actionVerbs) {
            if (verb.toLowerCase().equals(firstWord)) return true;
        }
        return false;
    }

    // ===== 5. REMOVE INDIAN-SPECIFIC ITEMS =====

    @SuppressWarnings("unchecked")
    private void removeIndianSpecificItems(Map<String, Object> enhanced) {
        // Remove unwanted fields from personalInfo
        Object personalInfoObj = enhanced.get("personalInfo");
        if (personalInfoObj instanceof Map) {
            Map<String, Object> personalInfo = (Map<String, Object>) personalInfoObj;
            FIELDS_TO_REMOVE.forEach(personalInfo::remove);
        }

        // Remove declaration section if present
        enhanced.remove("declaration");

        // Also check the Indian-specific rules from content service
        try {
            Map<String, String> indianItems = atsContentService.getIndianItemsToRemove();
            if (personalInfoObj instanceof Map) {
                Map<String, Object> personalInfo = (Map<String, Object>) personalInfoObj;
                for (String key : indianItems.keySet()) {
                    personalInfo.remove(key);
                }
            }
        } catch (Exception e) {
            // Content service might not be loaded yet — ignore silently
        }
    }

    // ===== 6. ENSURE ALL SECTIONS HAVE CONTENT =====

    private void ensureSectionContent(Map<String, Object> enhanced, boolean isFresher,
                                       List<String> matchedKeywords) {
        // If skills is empty, populate from matched keywords
        Object skillsObj = enhanced.get("skills");
        if (skillsObj == null || (skillsObj instanceof Map && ((Map<?, ?>) skillsObj).isEmpty())) {
            if (matchedKeywords != null && !matchedKeywords.isEmpty()) {
                Map<String, String> skills = new LinkedHashMap<>();
                skills.put("Technical Skills", String.join(", ", matchedKeywords));
                enhanced.put("skills", skills);
            }
        }

        // If experience is empty and candidate is fresher, remove the section
        Object expObj = enhanced.get("experience");
        if (expObj instanceof List && ((List<?>) expObj).isEmpty() && isFresher) {
            enhanced.remove("experience");
        }

        // If projects is empty, remove the section
        Object projObj = enhanced.get("projects");
        if (projObj instanceof List && ((List<?>) projObj).isEmpty()) {
            enhanced.remove("projects");
        }

        // Education should always exist — leave placeholder if empty
        Object eduObj = enhanced.get("education");
        if (eduObj == null || (eduObj instanceof List && ((List<?>) eduObj).isEmpty())) {
            List<Map<String, Object>> placeholder = new ArrayList<>();
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("degree", "");
            entry.put("institution", "");
            entry.put("year", "");
            entry.put("score", "");
            placeholder.add(entry);
            enhanced.put("education", placeholder);
        }
    }

    // ===== 7. DETERMINE FRESHER VS EXPERIENCED =====

    @SuppressWarnings("unchecked")
    private boolean determineIsFresher(Map<String, Object> enhanced) {
        Object expObj = enhanced.get("experience");
        if (!(expObj instanceof List)) return true;

        List<Map<String, Object>> experience = (List<Map<String, Object>>) expObj;
        if (experience.isEmpty()) return true;

        int totalYears = estimateYearsOfExperience(enhanced);
        return totalYears < 2;
    }

    @SuppressWarnings("unchecked")
    private int estimateYearsOfExperience(Map<String, Object> resumeData) {
        Object expObj = resumeData.get("experience");
        if (!(expObj instanceof List)) return 0;

        List<Map<String, Object>> experience = (List<Map<String, Object>>) expObj;
        int totalYears = 0;

        for (Map<String, Object> job : experience) {
            String dates = getString(job, "dates");
            if (dates == null) continue;

            Matcher matcher = YEAR_RANGE_PATTERN.matcher(dates);
            if (matcher.find()) {
                try {
                    int startYear = Integer.parseInt(matcher.group(1));
                    String endStr = matcher.group(2).toLowerCase();
                    int endYear;
                    if (endStr.contains("present") || endStr.contains("current") || endStr.contains("ongoing")) {
                        endYear = Calendar.getInstance().get(Calendar.YEAR);
                    } else {
                        endYear = Integer.parseInt(endStr);
                    }
                    totalYears += Math.max(0, endYear - startYear);
                } catch (NumberFormatException ignored) {
                    // skip
                }
            }
        }

        return totalYears;
    }

    // ===== 8. SUGGEST CERTIFICATIONS =====

    private void suggestCertifications(Map<String, Object> enhanced, String industry) {
        Object certsObj = enhanced.get("certifications");
        boolean hasCerts = certsObj instanceof List && !((List<?>) certsObj).isEmpty();

        if (!hasCerts) {
            List<String> suggested = atsContentService.getCertifications(industry);
            if (suggested != null && !suggested.isEmpty()) {
                // Take top 3
                List<String> top3 = suggested.subList(0, Math.min(3, suggested.size()));
                enhanced.put("suggestedCertifications", new ArrayList<>(top3));
            }
        }
    }

    // ===== UTILITY METHODS =====

    @SuppressWarnings("unchecked")
    private Map<String, Object> deepCopy(Map<String, Object> original) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                copy.put(entry.getKey(), deepCopy((Map<String, Object>) value));
            } else if (value instanceof List) {
                copy.put(entry.getKey(), deepCopyList((List<?>) value));
            } else {
                copy.put(entry.getKey(), value);
            }
        }
        return copy;
    }

    @SuppressWarnings("unchecked")
    private List<Object> deepCopyList(List<?> original) {
        List<Object> copy = new ArrayList<>();
        for (Object item : original) {
            if (item instanceof Map) {
                copy.add(deepCopy((Map<String, Object>) item));
            } else if (item instanceof List) {
                copy.add(deepCopyList((List<?>) item));
            } else {
                copy.add(item);
            }
        }
        return copy;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> data, String key) {
        Object val = data.get(key);
        if (val instanceof Map) return (Map<String, Object>) val;
        return new LinkedHashMap<>();
    }

    private String getString(Map<String, Object> data, String key) {
        Object val = data.get(key);
        return val != null ? String.valueOf(val) : null;
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
        return new ArrayList<>();
    }

    private String truncateToWords(String text, int maxWords) {
        if (text == null) return "";
        String[] words = text.split("\\s+");
        if (words.length <= maxWords) return text;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            if (i > 0) sb.append(" ");
            sb.append(words[i]);
        }
        // End with period if not already
        String result = sb.toString().trim();
        if (!result.endsWith(".")) result += ".";
        return result;
    }

    private String capitalizeWords(String text) {
        if (text == null || text.isBlank()) return text;
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (sb.length() > 0) sb.append(" ");
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) sb.append(word.substring(1));
            }
        }
        return sb.toString();
    }
}
