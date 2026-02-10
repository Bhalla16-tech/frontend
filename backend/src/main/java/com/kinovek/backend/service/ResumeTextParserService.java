package com.kinovek.backend.service;

import com.kinovek.backend.config.KeywordConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses raw resume text into a structured Map for PDF generation and analysis.
 * Fault-tolerant: never throws exceptions, returns empty values for unparseable sections.
 */
@Service
public class ResumeTextParserService {

    @Autowired
    private KeywordConfig keywordConfig;

    // ===== REGEX PATTERNS =====
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(\\+?\\d{1,3}[\\-.\\s]?)?\\(?\\d{3}\\)?[\\-.\\s]?\\d{3}[\\-.\\s]?\\d{4}");

    private static final Pattern LINKEDIN_PATTERN =
            Pattern.compile("(?:https?://)?(?:www\\.)?linkedin\\.com/in/[a-zA-Z0-9\\-_%]+/?", Pattern.CASE_INSENSITIVE);

    private static final Pattern YEAR_RANGE_PATTERN =
            Pattern.compile("(\\d{4})\\s*[-–—]\\s*(\\d{4}|[Pp]resent|[Cc]urrent|[Tt]ill\\s+[Dd]ate|[Oo]ngoing)");

    private static final Pattern SINGLE_YEAR_PATTERN =
            Pattern.compile("\\b(19|20)\\d{2}\\b");

    private static final Pattern CGPA_PATTERN =
            Pattern.compile("(?:CGPA|GPA|CPI)\\s*[:\\-]?\\s*\\d+\\.?\\d*\\s*/\\s*\\d+", Pattern.CASE_INSENSITIVE);

    private static final Pattern PERCENTAGE_PATTERN =
            Pattern.compile("\\d{2,3}\\.?\\d*\\s*%");

    private static final Pattern DEGREE_PATTERN = Pattern.compile(
            "\\b(B\\.?\\s?Tech|M\\.?\\s?Tech|B\\.?\\s?E|M\\.?\\s?E|B\\.?\\s?Sc|M\\.?\\s?Sc|" +
                    "BCA|MCA|B\\.?\\s?Com|M\\.?\\s?Com|BBA|MBA|B\\.?\\s?Pharm|M\\.?\\s?Pharm|" +
                    "B\\.?\\s?Arch|M\\.?\\s?Arch|B\\.?\\s?Des|M\\.?\\s?Des|" +
                    "Bachelor|Master|Ph\\.?\\s?D|Diploma|" +
                    "Bachelor of Technology|Bachelor of Engineering|Bachelor of Science|" +
                    "Master of Technology|Master of Engineering|Master of Science|" +
                    "Bachelor of Computer Applications|Master of Computer Applications|" +
                    "Bachelor of Business Administration|Master of Business Administration|" +
                    "Bachelor of Commerce|Master of Commerce)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern LOCATION_PATTERN =
            Pattern.compile("([A-Z][a-z]+(?:\\s[A-Z][a-z]+)?),\\s*([A-Z][a-z]+(?:\\s[A-Z][a-z]+)?)");

    // Common job title keywords to help identify experience entries
    private static final Set<String> JOB_TITLE_KEYWORDS = new HashSet<>(Arrays.asList(
            "engineer", "developer", "analyst", "manager", "intern", "lead", "architect",
            "designer", "consultant", "specialist", "administrator", "coordinator", "executive",
            "associate", "trainee", "officer", "head", "director", "vp", "president",
            "senior", "junior", "sr.", "jr.", "full stack", "frontend", "backend",
            "software", "data", "project", "product", "quality", "devops", "sre",
            "pharmacist", "technician", "supervisor", "assistant"
    ));

    // ===== MAIN PARSE METHOD =====

    /**
     * Parse raw resume text into a structured Map.
     *
     * @param resumeText raw text extracted from PDF/DOCX
     * @return structured resume data map
     */
    public Map<String, Object> parseResumeText(String resumeText) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (resumeText == null || resumeText.isBlank()) {
            return buildEmptyResult();
        }

        try {
            String[] lines = resumeText.split("\\r?\\n");

            // Step 1: Extract personal info from top of resume
            Map<String, Object> personalInfo = extractPersonalInfo(lines, resumeText);
            result.put("personalInfo", personalInfo);

            // Step 2: Detect and split sections
            Map<String, String> sections = detectAndSplitSections(lines);

            // Step 3: Parse each section
            result.put("summary", parseSummary(sections));
            result.put("education", parseEducation(sections));
            result.put("skills", parseSkills(sections));
            result.put("experience", parseExperience(sections));
            result.put("projects", parseProjects(sections));
            result.put("certifications", parseCertifications(sections));
            result.put("achievements", parseAchievements(sections));

        } catch (Exception e) {
            System.err.println("⚠️ Resume parsing encountered an error: " + e.getMessage());
            return buildEmptyResult();
        }

        return result;
    }

    // ===== PERSONAL INFO EXTRACTION =====

    private Map<String, Object> extractPersonalInfo(String[] lines, String fullText) {
        Map<String, Object> info = new LinkedHashMap<>();

        // Name: first non-empty, non-contact line
        String name = "";
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            // Skip if line looks like contact info
            if (EMAIL_PATTERN.matcher(trimmed).find()) continue;
            if (PHONE_PATTERN.matcher(trimmed).find()) continue;
            if (LINKEDIN_PATTERN.matcher(trimmed).find()) continue;
            if (isSectionHeader(trimmed)) continue;
            name = trimmed;
            break;
        }
        info.put("fullName", name);

        // Email
        Matcher emailMatcher = EMAIL_PATTERN.matcher(fullText);
        info.put("email", emailMatcher.find() ? emailMatcher.group() : "");

        // Phone
        Matcher phoneMatcher = PHONE_PATTERN.matcher(fullText);
        info.put("phone", phoneMatcher.find() ? phoneMatcher.group() : "");

        // LinkedIn
        Matcher linkedinMatcher = LINKEDIN_PATTERN.matcher(fullText);
        info.put("linkedin", linkedinMatcher.find() ? linkedinMatcher.group() : "");

        // Location — search in top ~10 lines
        String location = "";
        int searchLines = Math.min(lines.length, 10);
        for (int i = 0; i < searchLines; i++) {
            Matcher locMatcher = LOCATION_PATTERN.matcher(lines[i].trim());
            if (locMatcher.find()) {
                location = locMatcher.group();
                break;
            }
        }
        info.put("location", location);

        return info;
    }

    // ===== SECTION DETECTION =====

    /**
     * Detect section headers in resume lines and split content into named sections.
     * Uses sectionHeaders from keywords.json for matching.
     */
    private Map<String, String> detectAndSplitSections(String[] lines) {
        // Map: standardSectionName → content text
        Map<String, String> sections = new LinkedHashMap<>();
        Map<String, java.util.List<String>> allHeaders = keywordConfig.getSectionHeaders();

        // Build reverse lookup: variation → standard section name
        Map<String, String> headerLookup = new LinkedHashMap<>();
        for (Map.Entry<String, java.util.List<String>> entry : allHeaders.entrySet()) {
            String standardName = entry.getKey();
            for (String variation : entry.getValue()) {
                headerLookup.put(variation.toLowerCase().trim(), standardName);
            }
        }

        // Scan lines to find section boundaries
        java.util.List<int[]> sectionBoundaries = new ArrayList<>(); // [lineIndex, standardName hash]
        java.util.List<String> sectionNames = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String lineTrimmed = lines[i].trim();
            if (lineTrimmed.isEmpty()) continue;

            String detected = detectSectionHeader(lineTrimmed, headerLookup);
            if (detected != null) {
                sectionBoundaries.add(new int[]{i});
                sectionNames.add(detected);
            }
        }

        // Extract content between section boundaries
        for (int i = 0; i < sectionBoundaries.size(); i++) {
            int startLine = sectionBoundaries.get(i)[0] + 1; // skip the header line itself
            int endLine = (i + 1 < sectionBoundaries.size()) ? sectionBoundaries.get(i + 1)[0] : lines.length;

            StringBuilder content = new StringBuilder();
            for (int j = startLine; j < endLine; j++) {
                content.append(lines[j]).append("\n");
            }

            String sectionName = mapToStandardSection(sectionNames.get(i));
            // If section already exists, append (e.g., multiple experience sections)
            sections.merge(sectionName, content.toString().trim(), (old, newVal) -> old + "\n" + newVal);
        }

        return sections;
    }

    /**
     * Check if a line matches any known section header.
     * Line must be primarily the header (not a sentence containing the header word).
     */
    private String detectSectionHeader(String line, Map<String, String> headerLookup) {
        // Clean the line: remove colons, dashes, underscores, equals at the end
        String cleaned = line.replaceAll("[:\\-_=]+$", "").trim().toLowerCase();
        // Also try removing leading bullets/numbers
        cleaned = cleaned.replaceAll("^[\\d.•\\-*]+\\s*", "").trim();

        // Direct match
        if (headerLookup.containsKey(cleaned)) {
            return headerLookup.get(cleaned);
        }

        // Match if line is short (< 50 chars) and starts with a header
        if (line.length() < 50) {
            for (Map.Entry<String, String> entry : headerLookup.entrySet()) {
                if (cleaned.equals(entry.getKey()) ||
                        cleaned.startsWith(entry.getKey() + " ") ||
                        cleaned.startsWith(entry.getKey() + ":")) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Map section names from keywords.json to our standard internal names.
     */
    private String mapToStandardSection(String sectionName) {
        if (sectionName == null) return "other";
        String lower = sectionName.toLowerCase();
        return switch (lower) {
            case "summary", "objective", "profile" -> "summary";
            case "education" -> "education";
            case "skills", "technical skills", "core competencies" -> "skills";
            case "experience", "work experience", "employment" -> "experience";
            case "projects", "academic projects" -> "projects";
            case "certifications", "licenses" -> "certifications";
            case "achievements", "awards", "honors" -> "achievements";
            case "contact" -> "contact";
            default -> lower;
        };
    }

    private boolean isSectionHeader(String line) {
        if (line.length() > 50) return false;
        String cleaned = line.replaceAll("[:\\-_=]+$", "").trim().toLowerCase();
        Map<String, java.util.List<String>> allHeaders = keywordConfig.getSectionHeaders();
        for (java.util.List<String> variations : allHeaders.values()) {
            for (String v : variations) {
                if (cleaned.equals(v.toLowerCase().trim())) return true;
            }
        }
        return false;
    }

    // ===== SECTION PARSERS =====

    private String parseSummary(Map<String, String> sections) {
        String content = sections.getOrDefault("summary", "");
        return content.isBlank() ? "" : content.replaceAll("\\s+", " ").trim();
    }

    private java.util.List<Map<String, Object>> parseEducation(Map<String, String> sections) {
        java.util.List<Map<String, Object>> educationList = new ArrayList<>();
        String content = sections.getOrDefault("education", "");
        if (content.isBlank()) return educationList;

        String[] lines = content.split("\\r?\\n");

        Map<String, Object> currentEntry = null;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // Check if line contains a degree
            Matcher degreeMatcher = DEGREE_PATTERN.matcher(trimmed);
            if (degreeMatcher.find()) {
                // Save previous entry
                if (currentEntry != null) {
                    educationList.add(currentEntry);
                }
                currentEntry = new LinkedHashMap<>();
                currentEntry.put("degree", trimmed);
                currentEntry.put("institution", "");
                currentEntry.put("year", "");
                currentEntry.put("score", "");
                continue;
            }

            if (currentEntry == null) {
                // First line might be institution before degree
                currentEntry = new LinkedHashMap<>();
                currentEntry.put("degree", "");
                currentEntry.put("institution", trimmed);
                currentEntry.put("year", "");
                currentEntry.put("score", "");
                continue;
            }

            // Check for CGPA/percentage
            Matcher cgpaMatcher = CGPA_PATTERN.matcher(trimmed);
            Matcher pctMatcher = PERCENTAGE_PATTERN.matcher(trimmed);
            if (cgpaMatcher.find()) {
                currentEntry.put("score", cgpaMatcher.group());
            } else if (pctMatcher.find()) {
                currentEntry.put("score", pctMatcher.group() + " (Percentage)");
            }

            // Check for year range
            Matcher yearRangeMatcher = YEAR_RANGE_PATTERN.matcher(trimmed);
            Matcher singleYearMatcher = SINGLE_YEAR_PATTERN.matcher(trimmed);
            if (yearRangeMatcher.find()) {
                currentEntry.put("year", yearRangeMatcher.group());
            } else if (singleYearMatcher.find() && ((String) currentEntry.get("year")).isEmpty()) {
                currentEntry.put("year", singleYearMatcher.group());
            }

            // If institution is empty and line doesn't look like score/year, treat as institution
            if (((String) currentEntry.get("institution")).isEmpty() &&
                    !cgpaMatcher.find() && !trimmed.matches(".*\\d{4}.*")) {
                currentEntry.put("institution", trimmed);
            }
        }

        // Don't forget last entry
        if (currentEntry != null) {
            educationList.add(currentEntry);
        }

        return educationList;
    }

    private Map<String, String> parseSkills(Map<String, String> sections) {
        Map<String, String> skills = new LinkedHashMap<>();
        String content = sections.getOrDefault("skills", "");
        if (content.isBlank()) return skills;

        String[] lines = content.split("\\r?\\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // Try to find "Category: value1, value2" pattern
            int colonIdx = trimmed.indexOf(':');
            if (colonIdx > 0 && colonIdx < 40) {
                String category = trimmed.substring(0, colonIdx).trim();
                String values = trimmed.substring(colonIdx + 1).trim();

                // Remove leading bullet/dash
                category = category.replaceAll("^[•\\-*]\\s*", "").trim();

                if (!category.isEmpty() && !values.isEmpty()) {
                    skills.put(category, values);
                    continue;
                }
            }

            // Try pipe-separated: "Category | value1 | value2"
            if (trimmed.contains("|")) {
                String[] parts = trimmed.split("\\|");
                if (parts.length >= 2) {
                    String category = parts[0].trim().replaceAll("^[•\\-*]\\s*", "").trim();
                    StringBuilder values = new StringBuilder();
                    for (int i = 1; i < parts.length; i++) {
                        if (values.length() > 0) values.append(", ");
                        values.append(parts[i].trim());
                    }
                    if (!category.isEmpty()) {
                        skills.put(category, values.toString());
                        continue;
                    }
                }
            }

            // If no pattern matched, add as general skills
            String cleaned = trimmed.replaceAll("^[•\\-*]\\s*", "").trim();
            if (!cleaned.isEmpty()) {
                skills.merge("Skills", cleaned, (old, newVal) -> old + ", " + newVal);
            }
        }

        return skills;
    }

    private java.util.List<Map<String, Object>> parseExperience(Map<String, String> sections) {
        java.util.List<Map<String, Object>> experienceList = new ArrayList<>();
        String content = sections.getOrDefault("experience", "");
        if (content.isBlank()) return experienceList;

        String[] lines = content.split("\\r?\\n");
        Map<String, Object> currentJob = null;
        java.util.List<String> currentBullets = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // Check if this line might be a job title
            if (looksLikeJobTitle(trimmed)) {
                // Save previous job
                if (currentJob != null) {
                    currentJob.put("bullets", currentBullets);
                    experienceList.add(currentJob);
                }
                currentJob = new LinkedHashMap<>();
                currentJob.put("title", trimmed);
                currentJob.put("company", "");
                currentJob.put("location", "");
                currentJob.put("dates", "");
                currentBullets = new ArrayList<>();
                continue;
            }

            if (currentJob == null) {
                // Could be company line before title
                currentJob = new LinkedHashMap<>();
                currentJob.put("title", trimmed);
                currentJob.put("company", "");
                currentJob.put("location", "");
                currentJob.put("dates", "");
                currentBullets = new ArrayList<>();
                continue;
            }

            // Check for date range
            Matcher dateRangeMatcher = YEAR_RANGE_PATTERN.matcher(trimmed);
            if (dateRangeMatcher.find() && ((String) currentJob.get("dates")).isEmpty()) {
                currentJob.put("dates", trimmed);

                // If company is still empty and there's text before the date, it's the company line
                if (((String) currentJob.get("company")).isEmpty()) {
                    String beforeDate = trimmed.substring(0, dateRangeMatcher.start()).trim();
                    beforeDate = beforeDate.replaceAll("[,|\\-–—]+$", "").trim();
                    if (!beforeDate.isEmpty()) {
                        currentJob.put("company", beforeDate);
                        currentJob.put("dates", dateRangeMatcher.group());
                    }
                }
                continue;
            }

            // Check for bullet point
            if (trimmed.startsWith("•") || trimmed.startsWith("-") || trimmed.startsWith("*") ||
                    trimmed.matches("^\\d+\\.\\s.*")) {
                String bulletText = trimmed.replaceAll("^[•\\-*]\\s*", "")
                        .replaceAll("^\\d+\\.\\s*", "").trim();
                if (!bulletText.isEmpty()) {
                    currentBullets.add(bulletText);
                }
                continue;
            }

            // If company is empty, this line might be the company
            if (((String) currentJob.get("company")).isEmpty()) {
                // Extract location if present
                Matcher locMatcher = LOCATION_PATTERN.matcher(trimmed);
                if (locMatcher.find()) {
                    currentJob.put("location", locMatcher.group());
                    String companyPart = trimmed.substring(0, locMatcher.start()).trim();
                    companyPart = companyPart.replaceAll("[,|\\-–—]+$", "").trim();
                    if (!companyPart.isEmpty()) {
                        currentJob.put("company", companyPart);
                    }
                } else {
                    currentJob.put("company", trimmed);
                }
                continue;
            }

            // Otherwise treat as continuation of description / bullet without marker
            if (trimmed.length() > 20) {
                currentBullets.add(trimmed);
            }
        }

        // Don't forget last job
        if (currentJob != null) {
            currentJob.put("bullets", currentBullets);
            experienceList.add(currentJob);
        }

        return experienceList;
    }

    private java.util.List<Map<String, Object>> parseProjects(Map<String, String> sections) {
        java.util.List<Map<String, Object>> projectList = new ArrayList<>();
        String content = sections.getOrDefault("projects", "");
        if (content.isBlank()) return projectList;

        String[] lines = content.split("\\r?\\n");
        Map<String, Object> currentProject = null;
        java.util.List<String> currentBullets = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // Check for bullet point
            boolean isBullet = trimmed.startsWith("•") || trimmed.startsWith("-") ||
                    trimmed.startsWith("*") || trimmed.matches("^\\d+\\.\\s.*");

            if (isBullet && currentProject != null) {
                String bulletText = trimmed.replaceAll("^[•\\-*]\\s*", "")
                        .replaceAll("^\\d+\\.\\s*", "").trim();
                if (!bulletText.isEmpty()) {
                    currentBullets.add(bulletText);
                }
                continue;
            }

            // Check for tech stack indicator (line with | or "Tech Stack:" or "Technologies:")
            if (currentProject != null && ((String) currentProject.get("techStack")).isEmpty()) {
                String lower = trimmed.toLowerCase();
                if (lower.startsWith("tech") || lower.startsWith("technologies") ||
                        lower.startsWith("built with") || lower.startsWith("tools") ||
                        trimmed.contains("|")) {
                    String techStack = trimmed.replaceAll("(?i)^(tech\\s*stack|technologies|built\\s*with|tools used)\\s*[:\\-]?\\s*", "").trim();
                    currentProject.put("techStack", techStack);
                    continue;
                }
            }

            // Non-bullet, non-tech-stack line = probably a new project title
            if (!isBullet) {
                // Save previous project
                if (currentProject != null) {
                    currentProject.put("bullets", currentBullets);
                    projectList.add(currentProject);
                }
                currentProject = new LinkedHashMap<>();

                // Check if title contains tech stack separated by |
                if (trimmed.contains("|")) {
                    String[] parts = trimmed.split("\\|", 2);
                    currentProject.put("name", parts[0].trim());
                    currentProject.put("techStack", parts[1].trim());
                } else {
                    currentProject.put("name", trimmed);
                    currentProject.put("techStack", "");
                }
                currentBullets = new ArrayList<>();
            }
        }

        // Don't forget last project
        if (currentProject != null) {
            currentProject.put("bullets", currentBullets);
            projectList.add(currentProject);
        }

        return projectList;
    }

    private java.util.List<String> parseCertifications(Map<String, String> sections) {
        return parseSimpleList(sections.getOrDefault("certifications", ""));
    }

    private java.util.List<String> parseAchievements(Map<String, String> sections) {
        return parseSimpleList(sections.getOrDefault("achievements", ""));
    }

    /**
     * Parse a section that is a simple list of items (one per line or bullet).
     */
    private java.util.List<String> parseSimpleList(String content) {
        java.util.List<String> items = new ArrayList<>();
        if (content == null || content.isBlank()) return items;

        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // Remove bullet markers
            String cleaned = trimmed.replaceAll("^[•\\-*]\\s*", "")
                    .replaceAll("^\\d+\\.\\s*", "").trim();
            if (!cleaned.isEmpty()) {
                items.add(cleaned);
            }
        }
        return items;
    }

    // ===== HELPER METHODS =====

    private boolean looksLikeJobTitle(String line) {
        String lower = line.toLowerCase();
        // Must not start with bullet
        if (lower.startsWith("•") || lower.startsWith("-") || lower.startsWith("*")) return false;
        // Must be reasonably short
        if (line.length() > 80) return false;
        // Check if any job title keyword is present
        for (String keyword : JOB_TITLE_KEYWORDS) {
            if (lower.contains(keyword)) return true;
        }
        return false;
    }

    private Map<String, Object> buildEmptyResult() {
        Map<String, Object> result = new LinkedHashMap<>();

        Map<String, Object> personalInfo = new LinkedHashMap<>();
        personalInfo.put("fullName", "");
        personalInfo.put("email", "");
        personalInfo.put("phone", "");
        personalInfo.put("linkedin", "");
        personalInfo.put("location", "");
        result.put("personalInfo", personalInfo);

        result.put("summary", "");
        result.put("education", new ArrayList<>());
        result.put("skills", new LinkedHashMap<>());
        result.put("experience", new ArrayList<>());
        result.put("projects", new ArrayList<>());
        result.put("certifications", new ArrayList<>());
        result.put("achievements", new ArrayList<>());

        return result;
    }
}
