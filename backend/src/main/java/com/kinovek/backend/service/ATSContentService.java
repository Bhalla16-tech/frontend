package com.kinovek.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;

@Service
public class ATSContentService {

    private static final Logger log = LoggerFactory.getLogger(ATSContentService.class);

    private JsonNode content;

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getClassLoader().getResourceAsStream("ats_resume_content.json");
            if (is == null) {
                throw new RuntimeException("ats_resume_content.json not found in resources!");
            }
            content = mapper.readTree(is);
            System.out.println("✅ ATS Resume Content Database loaded successfully!");
            System.out.println("   Industries: " + content.at("/summaryTemplates").size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load ATS Content: " + e.getMessage(), e);
        }
    }

    // ===== DETECT INDUSTRY FROM JOB DESCRIPTION =====
    public String detectIndustry(String jobDescription) {
        log.info("=== INDUSTRY DETECTION: JD length={} ===", jobDescription.length());
        String jdLower = jobDescription.toLowerCase();

        Map<String, List<String>> industryKeywords = new LinkedHashMap<>();
        industryKeywords.put("IT_Software", Arrays.asList("software", "developer", "programming", "java", "python", "javascript", "react", "angular", "spring boot", "node.js", "full stack", "backend", "frontend", "devops", "cloud", "aws", "api", "microservices", "web development", "mobile app", "database", "sql", "agile", "scrum"));
        industryKeywords.put("Data_Science_AI", Arrays.asList("data scientist", "machine learning", "deep learning", "artificial intelligence", "nlp", "natural language", "tensorflow", "pytorch", "data analyst", "data engineer", "big data", "spark", "hadoop", "tableau", "power bi", "statistics", "predictive model"));
        industryKeywords.put("Mechanical_Engineering", Arrays.asList("mechanical engineer", "solidworks", "catia", "autocad", "cad design", "fea", "cfd", "thermodynamics", "manufacturing", "cnc", "gd&t", "hvac", "machine design", "product design", "ansys"));
        industryKeywords.put("Civil_Engineering", Arrays.asList("civil engineer", "structural", "construction", "site engineer", "staad pro", "etabs", "revit", "bim", "surveying", "rcc design", "quantity surveying", "primavera", "transportation", "geotechnical"));
        industryKeywords.put("Electrical_Engineering", Arrays.asList("electrical engineer", "plc", "scada", "power systems", "control systems", "electrical design", "automation", "panel design", "substation", "renewable energy", "solar", "etap", "switchgear"));
        industryKeywords.put("Electronics_Communication", Arrays.asList("electronics engineer", "embedded", "pcb", "vlsi", "fpga", "microcontroller", "iot", "firmware", "signal processing", "rf engineer", "antenna", "communication systems", "verilog", "vhdl", "arm"));
        industryKeywords.put("Pharmacy", Arrays.asList("pharmacist", "pharmacy", "pharmaceutical", "drug", "clinical", "formulation", "hplc", "gmp", "pharmacovigilance", "regulatory affairs", "drug safety", "quality control", "prescription", "patient counseling", "pharmacology"));
        industryKeywords.put("UI_UX_Design", Arrays.asList("ui/ux", "ux designer", "ui designer", "user experience", "user interface", "figma", "wireframe", "prototype", "usability", "user research", "information architecture", "interaction design", "design system"));
        industryKeywords.put("Graphic_Design_VFX", Arrays.asList("graphic designer", "photoshop", "illustrator", "indesign", "logo design", "branding", "vfx", "animation", "3d modeling", "maya", "blender", "after effects", "motion graphics", "video editing", "premiere pro", "nuke"));
        industryKeywords.put("Digital_Marketing", Arrays.asList("digital marketing", "seo", "sem", "google ads", "social media", "content marketing", "email marketing", "ppc", "facebook ads", "analytics", "marketing automation", "hubspot", "copywriting"));

        String bestMatch = "IT_Software";
        int highestScore = 0;

        for (Map.Entry<String, List<String>> entry : industryKeywords.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (jdLower.contains(keyword)) {
                    score++;
                }
            }
            if (score > highestScore) {
                highestScore = score;
                bestMatch = entry.getKey();
            }
        }
        log.info("=== INDUSTRY DETECTION: Result={} (score={}) ===", bestMatch, highestScore);
        return bestMatch;
    }

    // ===== GET SUMMARY TEMPLATES =====
    public List<String> getSummaryTemplates(String industry, boolean isFresher) {
        String level = isFresher ? "fresher" : "experienced";
        JsonNode templates = content.at("/summaryTemplates/" + industry + "/" + level);
        List<String> result = new ArrayList<>();
        if (templates.isArray()) {
            for (JsonNode t : templates) {
                result.add(t.asText());
            }
        }
        return result;
    }

    // ===== GET BULLET TEMPLATES =====
    public List<String> getBulletTemplates(String industry, boolean isFresher) {
        String type = isFresher ? "fresher_projects" : "experienced_work";
        JsonNode bullets = content.at("/bulletTemplates/" + industry + "/" + type);
        List<String> result = new ArrayList<>();
        if (bullets.isArray()) {
            for (JsonNode b : bullets) {
                result.add(b.asText());
            }
        }
        return result;
    }

    // ===== GET ACTION VERBS =====
    public List<String> getAllActionVerbs(String industry) {
        List<String> allVerbs = new ArrayList<>();
        JsonNode industryVerbs = content.at("/actionVerbsByIndustry/" + industry);
        if (industryVerbs.isObject()) {
            industryVerbs.fields().forEachRemaining(entry -> {
                for (JsonNode v : entry.getValue()) {
                    allVerbs.add(v.asText());
                }
            });
        }
        return allVerbs;
    }

    // ===== GET CERTIFICATIONS =====
    public List<String> getCertifications(String industry) {
        JsonNode certs = content.at("/certificationsByIndustry/" + industry);
        List<String> result = new ArrayList<>();
        if (certs.isArray()) {
            for (JsonNode c : certs) {
                result.add(c.asText());
            }
        }
        return result;
    }

    // ===== GET EDUCATION FORMATS =====
    public String getFullDegreeName(String abbreviation) {
        return content.at("/educationFormats/degreeAbbreviations/" + abbreviation).asText(abbreviation);
    }

    public List<String> getRelevantCoursework(String industry) {
        JsonNode courses = content.at("/educationFormats/relevantCoursework/" + industry);
        List<String> result = new ArrayList<>();
        if (courses.isArray()) {
            for (JsonNode c : courses) {
                result.add(c.asText());
            }
        }
        return result;
    }

    // ===== GET BANNED PHRASES (for summary) =====
    public List<String> getBannedSummaryPhrases() {
        JsonNode banned = content.at("/sectionContentRules/summary/neverUse");
        List<String> result = new ArrayList<>();
        if (banned.isArray()) {
            for (JsonNode b : banned) {
                result.add(b.asText());
            }
        }
        return result;
    }

    // ===== GET BANNED BULLET STARTERS =====
    public List<String> getBannedBulletStarters() {
        JsonNode banned = content.at("/sectionContentRules/experience/neverStartWith");
        List<String> result = new ArrayList<>();
        if (banned.isArray()) {
            for (JsonNode b : banned) {
                result.add(b.asText().toLowerCase());
            }
        }
        return result;
    }

    // ===== GET INDIAN RESUME ITEMS TO REMOVE =====
    public Map<String, String> getIndianItemsToRemove() {
        Map<String, String> items = new LinkedHashMap<>();
        JsonNode indian = content.at("/indianResumeSpecifics");
        if (indian.isObject()) {
            indian.fields().forEachRemaining(entry -> {
                items.put(entry.getKey(), entry.getValue().asText());
            });
        }
        return items;
    }

    // ===== GET SUMMARY START WORDS =====
    public List<String> getSummaryStartWords() {
        JsonNode words = content.at("/sectionContentRules/summary/startWith");
        List<String> result = new ArrayList<>();
        if (words.isArray()) {
            for (JsonNode w : words) {
                result.add(w.asText());
            }
        }
        return result;
    }
}
