package com.kinovek.backend.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Loads keywords.json at application startup and provides
 * skill categories, synonym mappings, and section headers
 * to the rest of the application.
 */
@Component
public class KeywordConfig {

    private static final Logger log = LoggerFactory.getLogger(KeywordConfig.class);

    /** All known skill/keyword strings (lowercased) mapped to their canonical display form. */
    private final Set<String> allSkillsLower = new HashSet<>();
    private final Map<String, String> skillDisplayForm = new HashMap<>();

    /**
     * Bidirectional synonym lookup.
     * Maps every synonym (lowercased) → canonical form,
     * AND canonical (lowercased) → canonical form.
     */
    private final Map<String, String> synonymToCanonical = new HashMap<>();

    /**
     * Canonical → set of all synonyms (lowercased), including itself.
     * Used for reverse lookup when checking if resume contains any form.
     */
    private final Map<String, Set<String>> canonicalToAllForms = new HashMap<>();

    /** Multi-word skills sorted longest-first for greedy matching. */
    private final List<String> multiWordSkills = new ArrayList<>();

    /** Section header name → list of variations (lowercased). */
    private final Map<String, List<String>> sectionHeaders = new LinkedHashMap<>();

    /** Section header name list in order. */
    private final List<String> requiredSections = new ArrayList<>();

    @PostConstruct
    public void init() {
        try {
            log.info("Loading keywords.json ...");
            ClassPathResource resource = new ClassPathResource("keywords.json");
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            JsonObject root = new Gson().fromJson(reader, JsonObject.class);
            reader.close();

            loadSkillCategories(root.getAsJsonObject("skillCategories"));
            loadSynonymMap(root.getAsJsonObject("synonymMap"));
            loadSectionHeaders(root.getAsJsonObject("sectionHeaders"));

            // Sort multi-word skills longest first so greedy matching works
            multiWordSkills.sort((a, b) -> Integer.compare(b.length(), a.length()));

            log.info("keywords.json loaded — {} skills, {} synonym groups, {} section types",
                    allSkillsLower.size(), canonicalToAllForms.size(), sectionHeaders.size());
        } catch (Exception e) {
            log.error("Failed to load keywords.json", e);
            throw new RuntimeException("Could not load keywords.json", e);
        }
    }

    // ──────────────────────── loaders ────────────────────────

    private void loadSkillCategories(JsonObject categories) {
        if (categories == null) return;
        for (String category : categories.keySet()) {
            JsonArray arr = categories.getAsJsonArray(category);
            for (JsonElement el : arr) {
                String skill = el.getAsString().trim();
                registerSkill(skill);
            }
        }
    }

    private void loadSynonymMap(JsonObject synMap) {
        if (synMap == null) return;
        for (String canonical : synMap.keySet()) {
            String canonicalTrimmed = canonical.trim();
            String canonicalLower = canonicalTrimmed.toLowerCase();

            // Register canonical itself as a skill
            registerSkill(canonicalTrimmed);
            synonymToCanonical.put(canonicalLower, canonicalTrimmed);

            Set<String> forms = canonicalToAllForms
                    .computeIfAbsent(canonicalTrimmed, k -> new HashSet<>());
            forms.add(canonicalLower);

            JsonArray synonyms = synMap.getAsJsonArray(canonical);
            for (JsonElement el : synonyms) {
                String syn = el.getAsString().trim();
                String synLower = syn.toLowerCase();
                synonymToCanonical.put(synLower, canonicalTrimmed);
                forms.add(synLower);
                registerSkill(syn);
            }
        }
    }

    private void loadSectionHeaders(JsonObject headers) {
        if (headers == null) return;
        for (String section : headers.keySet()) {
            List<String> variations = new ArrayList<>();
            JsonArray arr = headers.getAsJsonArray(section);
            for (JsonElement el : arr) {
                variations.add(el.getAsString().trim().toLowerCase());
            }
            sectionHeaders.put(section, variations);
            requiredSections.add(section);
        }
    }

    private void registerSkill(String skill) {
        String lower = skill.toLowerCase();
        allSkillsLower.add(lower);

        // Keep the first properly-cased version; don't let all-lowercase
        // synonyms overwrite an already-registered display form.
        // e.g., keep "AWS" even when "aws" is also registered as a synonym.
        String existing = skillDisplayForm.get(lower);
        if (existing == null) {
            skillDisplayForm.put(lower, skill);
        } else if (!skill.equals(skill.toLowerCase()) && existing.equals(existing.toLowerCase())) {
            // New form has mixed/upper case, existing is all lowercase → upgrade
            skillDisplayForm.put(lower, skill);
        }

        if (skill.contains(" ") || skill.contains("-") || skill.contains("/") || skill.contains(".")) {
            if (!multiWordSkills.contains(lower)) {
                multiWordSkills.add(lower);
            }
        }
    }

    // ──────────────────────── public API ────────────────────────

    /** Returns true if the word/phrase is a known skill (case-insensitive). */
    public boolean isKnownSkill(String text) {
        return allSkillsLower.contains(text.toLowerCase());
    }

    /** Returns the display form of a known skill, or null. */
    public String getDisplayForm(String text) {
        String canonical = synonymToCanonical.get(text.toLowerCase());
        if (canonical != null) return canonical;
        return skillDisplayForm.get(text.toLowerCase());
    }

    /**
     * Returns the originally registered display form for a skill (from skillDisplayForm map).
     * Unlike getDisplayForm(), this does NOT resolve through synonymToCanonical.
     * e.g., "kafka" → "Kafka" (not "Apache Kafka")
     *        "aws" → "AWS" (not "Amazon Web Services")
     *        "ci/cd" → "CI/CD" (not "Continuous Deployment")
     */
    public String getRegisteredForm(String text) {
        return skillDisplayForm.get(text.toLowerCase());
    }

    /**
     * Returns the canonical form for a skill or synonym.
     * e.g. "React.js" → "React", "AWS" → "Amazon Web Services"
     * Returns the display form if no synonym mapping, or null if unknown.
     */
    public String getCanonical(String text) {
        String canonical = synonymToCanonical.get(text.toLowerCase());
        if (canonical != null) return canonical;
        if (allSkillsLower.contains(text.toLowerCase())) {
            return skillDisplayForm.get(text.toLowerCase());
        }
        return null;
    }

    /**
     * Returns all known forms (lowercased) for a canonical skill.
     * Includes the canonical itself and all synonyms.
     */
    public Set<String> getAllForms(String canonical) {
        Set<String> forms = canonicalToAllForms.get(canonical);
        if (forms != null) return forms;
        // If the input was a synonym, resolve to canonical first
        String resolved = synonymToCanonical.get(canonical.toLowerCase());
        if (resolved != null) {
            return canonicalToAllForms.getOrDefault(resolved, Set.of(canonical.toLowerCase()));
        }
        return Set.of(canonical.toLowerCase());
    }

    /** Multi-word skills sorted longest-first. */
    public List<String> getMultiWordSkills() {
        return Collections.unmodifiableList(multiWordSkills);
    }

    /** Section name → list of header variations (lowercased). */
    public Map<String, List<String>> getSectionHeaders() {
        return Collections.unmodifiableMap(sectionHeaders);
    }

    /** Ordered list of section names from the JSON (e.g. "summary", "experience", ...). */
    public List<String> getRequiredSections() {
        return Collections.unmodifiableList(requiredSections);
    }

    /** The full set of known skills (lowercased). */
    public Set<String> getAllSkillsLower() {
        return Collections.unmodifiableSet(allSkillsLower);
    }
}
