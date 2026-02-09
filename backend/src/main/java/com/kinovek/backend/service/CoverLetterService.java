package com.kinovek.backend.service;

import com.kinovek.backend.model.CoverLetterResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CoverLetterService {

    @Autowired
    private ResumeParserService resumeParserService;

    /**
     * Generates a professional cover letter from resume + job description.
     */
    public CoverLetterResult generateCoverLetter(MultipartFile resumeFile, String jobDescription) throws IOException {
        String resumeText = resumeParserService.parseResume(resumeFile);

        // Extract information from resume
        String candidateName = extractName(resumeText);
        List<String> skills = extractSkills(resumeText);
        String experienceSummary = extractExperienceSummary(resumeText);

        // Extract information from job description
        String companyName = extractCompanyName(jobDescription);
        String roleName = extractRoleName(jobDescription);
        List<String> requirements = extractRequirements(jobDescription);

        // Find overlapping skills with requirements
        List<String> relevantSkills = findRelevantSkills(skills, requirements);

        // Generate the cover letter
        String coverLetterText = buildCoverLetter(
                candidateName, companyName, roleName,
                relevantSkills, experienceSummary, requirements);

        CoverLetterResult result = new CoverLetterResult();
        result.setCandidateName(candidateName);
        result.setCompanyName(companyName);
        result.setTargetRole(roleName);
        result.setCoverLetterText(coverLetterText);
        return result;
    }

    // ==================== Resume Extraction ====================

    private String extractName(String resumeText) {
        String[] lines = resumeText.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            // Skip lines that look like section headers or contact info
            if (trimmed.toLowerCase().matches(".*(summary|experience|education|skills|objective|phone|email|address|http).*")) continue;
            // Name is typically the first non-empty, non-header line (2-4 words)
            String[] words = trimmed.split("\\s+");
            if (words.length >= 1 && words.length <= 5 && trimmed.length() < 40) {
                return trimmed;
            }
        }
        return "[Your Name]";
    }

    private List<String> extractSkills(String resumeText) {
        List<String> skills = new ArrayList<>();
        String lower = resumeText.toLowerCase();

        // Try to find a "Skills" section
        Pattern skillSection = Pattern.compile("(?i)(skills|technical skills|core competencies|key skills)[:\\s]*\n([\\s\\S]*?)(?=\n(?:experience|education|projects|certifications|awards|references|$))", Pattern.MULTILINE);
        Matcher m = skillSection.matcher(resumeText);

        if (m.find()) {
            String skillsBlock = m.group(2);
            // Split by common delimiters
            String[] tokens = skillsBlock.split("[,|\\n\\-\u2022•]+");
            for (String token : tokens) {
                String trimmed = token.trim();
                if (!trimmed.isEmpty() && trimmed.length() < 40) {
                    skills.add(trimmed);
                }
            }
        }

        // Fallback: scan for common tech keywords
        if (skills.isEmpty()) {
            String[] commonSkills = {"Java", "Python", "JavaScript", "TypeScript", "React", "Angular", "Vue",
                    "Spring Boot", "Node.js", "SQL", "AWS", "Azure", "Docker", "Kubernetes",
                    "Git", "REST API", "Machine Learning", "Data Analysis", "Agile", "Scrum",
                    "HTML", "CSS", "C++", "C#", ".NET", "MongoDB", "PostgreSQL", "MySQL",
                    "Communication", "Leadership", "Project Management", "Problem Solving"};
            for (String skill : commonSkills) {
                if (lower.contains(skill.toLowerCase())) {
                    skills.add(skill);
                }
            }
        }

        return skills.size() > 8 ? skills.subList(0, 8) : skills;
    }

    private String extractExperienceSummary(String resumeText) {
        // Try to find years of experience
        Pattern yearsPattern = Pattern.compile("(\\d+)\\+?\\s*(?:years?|yrs?)\\s*(?:of)?\\s*(?:experience|exp)", Pattern.CASE_INSENSITIVE);
        Matcher m = yearsPattern.matcher(resumeText);
        if (m.find()) {
            return m.group(1) + "+ years of professional experience";
        }

        // Count job entries as a rough estimate
        Pattern datePattern = Pattern.compile("(20\\d{2}|19\\d{2})\\s*[-–]\\s*(20\\d{2}|19\\d{2}|present|current)", Pattern.CASE_INSENSITIVE);
        Matcher dateMatcher = datePattern.matcher(resumeText);
        int jobCount = 0;
        while (dateMatcher.find()) jobCount++;

        if (jobCount > 0) {
            return "experience across " + jobCount + " professional role" + (jobCount > 1 ? "s" : "");
        }

        return "relevant professional experience";
    }

    // ==================== JD Extraction ====================

    private String extractCompanyName(String jd) {
        // Common patterns: "at [Company]", "[Company] is", "About [Company]", "Join [Company]"
        String[] patterns = {
                "(?i)(?:at|join|about)\\s+([A-Z][A-Za-z0-9&'.\\s]{1,30}?)(?:\\s*(?:is|are|we|,|\\.|\\n|$))",
                "(?i)company\\s*[:=]\\s*(.+?)(?:\n|$)",
                "(?i)^([A-Z][A-Za-z0-9&'.\\s]{2,25})\\s*$"
        };
        for (String pattern : patterns) {
            Matcher m = Pattern.compile(pattern, Pattern.MULTILINE).matcher(jd);
            if (m.find()) {
                return m.group(1).trim();
            }
        }
        return "[Company Name]";
    }

    private String extractRoleName(String jd) {
        String[] patterns = {
                "(?i)(?:job title|position|role)\\s*[:=]\\s*(.+?)(?:\n|$)",
                "(?i)(?:hiring|looking for|seeking)\\s+(?:a|an)?\\s*(.+?)(?:\\s+(?:proficient|with|who|at|for|in|to|\\.|,|\\n|$))",
                "(?i)^(.+?(?:engineer|developer|manager|analyst|designer|architect|scientist|specialist|coordinator|consultant|lead|director|intern))\\s*$"
        };
        for (String pattern : patterns) {
            Matcher m = Pattern.compile(pattern, Pattern.MULTILINE).matcher(jd);
            if (m.find()) {
                String role = m.group(1).trim();
                // Remove trailing articles/prepositions
                role = role.replaceAll("\\s+(?:a|an|the|who|with|at|for|in|to)$", "").trim();
                if (role.length() >= 3 && role.length() < 60) return role;
            }
        }
        return "[Position Title]";
    }

    private List<String> extractRequirements(String jd) {
        List<String> reqs = new ArrayList<>();
        // Look for requirements/qualifications section
        Pattern reqSection = Pattern.compile("(?i)(requirements|qualifications|what we.re looking for|must have|you.ll need)[:\\s]*\n([\\s\\S]*?)(?=\n(?:benefits|perks|about|how to|application|$))", Pattern.MULTILINE);
        Matcher m = reqSection.matcher(jd);

        if (m.find()) {
            String block = m.group(2);
            String[] lines = block.split("\n");
            for (String line : lines) {
                String trimmed = line.replaceAll("^[\\-\u2022•*>]+", "").trim();
                if (!trimmed.isEmpty() && trimmed.length() > 5 && trimmed.length() < 100) {
                    reqs.add(trimmed);
                }
            }
        }

        // Fallback: extract key skill phrases rather than raw JD text
        if (reqs.isEmpty()) {
            String[] techTerms = {"Java", "Python", "JavaScript", "TypeScript", "React", "Angular", "Vue",
                    "Spring Boot", "Node.js", "SQL", "AWS", "Azure", "Docker", "Kubernetes",
                    "CI/CD", "microservices", "REST API", "Machine Learning", "DevOps",
                    "Agile", "Scrum", "Git", "cloud", "containerization", "data analysis",
                    "C++", "C#", ".NET", "Go", "Kotlin", "Swift", "GraphQL", "Redis",
                    "Kafka", "Terraform", "Jenkins", "HTML", "CSS", "MongoDB", "PostgreSQL"};
            String jdLower = jd.toLowerCase();
            for (String term : techTerms) {
                if (jdLower.contains(term.toLowerCase())) {
                    reqs.add("proficiency in " + term);
                    if (reqs.size() >= 6) break;
                }
            }
        }

        return reqs.size() > 6 ? reqs.subList(0, 6) : reqs;
    }

    // ==================== Matching ====================

    private List<String> findRelevantSkills(List<String> skills, List<String> requirements) {
        List<String> relevant = new ArrayList<>();
        String reqText = String.join(" ", requirements).toLowerCase();

        for (String skill : skills) {
            if (reqText.contains(skill.toLowerCase())) {
                relevant.add(skill);
            }
        }
        // If no overlap found, return top skills anyway
        if (relevant.isEmpty()) {
            return skills.size() > 4 ? skills.subList(0, 4) : skills;
        }
        return relevant;
    }

    // ==================== Cover Letter Builder ====================

    private String buildCoverLetter(String name, String company, String role,
                                     List<String> relevantSkills, String experience,
                                     List<String> requirements) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        String skillsList = String.join(", ", relevantSkills);

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append(name).append("\n");
        sb.append(today).append("\n\n");

        // Salutation
        sb.append("Dear Hiring Manager,\n\n");

        // Opening paragraph
        sb.append("I am writing to express my strong interest in the ");
        sb.append(role).append(" position at ").append(company);
        sb.append(". With ").append(experience);
        sb.append(", I am confident that my background and skills make me an excellent fit for this role.\n\n");

        // Skills paragraph
        if (!relevantSkills.isEmpty()) {
            sb.append("Throughout my career, I have developed strong expertise in ");
            sb.append(skillsList);
            sb.append(". These skills directly align with the requirements outlined in your job description");
            sb.append(" and would allow me to make an immediate and meaningful contribution to your team.\n\n");
        }

        // Requirements alignment paragraph
        if (!requirements.isEmpty() && requirements.size() >= 2) {
            sb.append("I am particularly drawn to this opportunity because it requires expertise in areas where I have proven results. ");
            sb.append("My experience includes ");
            // Extract just the tech term from "proficiency in X" format
            List<String> cleanReqs = new ArrayList<>();
            for (String req : requirements) {
                String clean = req.replaceAll("(?i)^proficiency in\\s+", "").trim();
                cleanReqs.add(clean);
            }
            if (cleanReqs.size() == 1) {
                sb.append(cleanReqs.get(0));
            } else if (cleanReqs.size() == 2) {
                sb.append(cleanReqs.get(0)).append(" and ").append(cleanReqs.get(1));
            } else {
                for (int i = 0; i < Math.min(cleanReqs.size(), 3); i++) {
                    if (i > 0) sb.append(", ");
                    if (i == Math.min(cleanReqs.size(), 3) - 1) sb.append("and ");
                    sb.append(cleanReqs.get(i));
                }
            }
            sb.append(", which I believe are critical to succeeding in this position.\n\n");
        }

        // Motivation paragraph
        sb.append("I am excited about the opportunity to bring my unique blend of skills and experience to ");
        sb.append(company);
        sb.append(". I am eager to contribute to your team's success and am confident that my proactive approach ");
        sb.append("and dedication to excellence would be a valuable asset.\n\n");

        // Closing
        sb.append("Thank you for considering my application. I would welcome the opportunity to discuss how my ");
        sb.append("qualifications align with your needs. I look forward to hearing from you.\n\n");

        sb.append("Sincerely,\n");
        sb.append(name);

        return sb.toString();
    }
}
