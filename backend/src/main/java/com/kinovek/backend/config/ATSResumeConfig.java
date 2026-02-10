package com.kinovek.backend.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Component
public class ATSResumeConfig {

    private JsonNode config;

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getClassLoader().getResourceAsStream("ats_resume_config.json");
            if (is == null) {
                throw new RuntimeException("ats_resume_config.json not found in resources!");
            }
            config = mapper.readTree(is);
            System.out.println("✅ ATS Resume Config loaded successfully!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load ATS Resume Config: " + e.getMessage(), e);
        }
    }

    // ===== PDF SETTINGS =====
    public float getMarginTop() { return config.at("/pdfSettings/margins/top").floatValue(); }
    public float getMarginBottom() { return config.at("/pdfSettings/margins/bottom").floatValue(); }
    public float getMarginLeft() { return config.at("/pdfSettings/margins/left").floatValue(); }
    public float getMarginRight() { return config.at("/pdfSettings/margins/right").floatValue(); }
    public String getTextColor() { return config.at("/pdfSettings/textColor").asText(); }
    public String getHeadingAccentColor() { return config.at("/pdfSettings/headingAccentColor").asText(); }
    public String getLineColor() { return config.at("/pdfSettings/lineColor").asText(); }

    // ===== FONT SIZES =====
    public float getNameFontSize() { return config.at("/fontSizes/candidateName/size").floatValue(); }
    public float getContactFontSize() { return config.at("/fontSizes/contactInfo/size").floatValue(); }
    public float getSectionHeadingSize() { return config.at("/fontSizes/sectionHeading/size").floatValue(); }
    public float getJobTitleSize() { return config.at("/fontSizes/jobTitle/size").floatValue(); }
    public float getBodyTextSize() { return config.at("/fontSizes/bodyText/size").floatValue(); }
    public float getCompanyNameSize() { return config.at("/fontSizes/companyName/size").floatValue(); }
    public float getDateSize() { return config.at("/fontSizes/dateRange/size").floatValue(); }
    public float getSkillCategorySize() { return config.at("/fontSizes/skillCategory/size").floatValue(); }
    public float getSkillValuesSize() { return config.at("/fontSizes/skillValues/size").floatValue(); }
    public float getProjectTitleSize() { return config.at("/fontSizes/projectTitle/size").floatValue(); }

    // ===== SPACING =====
    public float getLineSpacing() { return config.at("/spacing/lineSpacing").floatValue(); }
    public float getAfterName() { return config.at("/spacing/afterCandidateName").floatValue(); }
    public float getAfterContact() { return config.at("/spacing/afterContactInfo").floatValue(); }
    public float getBeforeSectionHeading() { return config.at("/spacing/beforeSectionHeading").floatValue(); }
    public float getAfterSectionHeading() { return config.at("/spacing/afterSectionHeading").floatValue(); }
    public float getBetweenBullets() { return config.at("/spacing/betweenBulletPoints").floatValue(); }
    public float getBetweenJobs() { return config.at("/spacing/betweenJobEntries").floatValue(); }
    public float getBetweenProjects() { return config.at("/spacing/betweenProjectEntries").floatValue(); }

    // ===== SECTION ORDER =====
    public JsonNode getFresherSectionOrder() { return config.at("/sectionOrder/fresher"); }
    public JsonNode getExperiencedSectionOrder() { return config.at("/sectionOrder/experienced"); }

    // ===== SECTION HEADINGS =====
    public String getSectionHeadingText(String key) {
        return config.at("/sectionHeadings/" + key).asText(key);
    }

    // ===== BULLET SETTINGS =====
    public String getBulletSymbol() { return config.at("/bulletPointFormat/symbol").asText("•"); }
    public float getBulletIndentation() { return config.at("/bulletPointFormat/indentation").floatValue(); }

    // ===== FILE NAMING =====
    public String getFileNamePattern() { return config.at("/fileNaming/pattern").asText("{FirstName}_{LastName}_Resume.pdf"); }
}
