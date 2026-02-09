package com.kinovek.backend.util;

import java.util.*;

public class KeywordMatcher {

    // Synonym map: each group of synonyms maps to a canonical form
    private static final Map<String, String> SYNONYM_MAP = new HashMap<>();

    static {
        // JavaScript synonyms
        addSynonyms("JavaScript", "JS", "Javascript", "javascript", "ECMAScript");
        // React synonyms
        addSynonyms("React", "React.js", "ReactJS", "react.js");
        // Node synonyms
        addSynonyms("Node.js", "NodeJS", "Node", "node.js");
        // TypeScript synonyms
        addSynonyms("TypeScript", "TS", "Typescript", "typescript");
        // Angular synonyms
        addSynonyms("Angular", "AngularJS", "Angular.js");
        // Vue synonyms
        addSynonyms("Vue", "Vue.js", "VueJS", "vue.js");
        // Python synonyms
        addSynonyms("Python", "python3", "Python3");
        // Java synonyms
        addSynonyms("Java", "java", "Core Java");
        // Spring Boot synonyms
        addSynonyms("Spring Boot", "SpringBoot", "Spring-Boot");
        // SQL synonyms
        addSynonyms("SQL", "MySQL", "PostgreSQL", "Postgres");
        // NoSQL synonyms
        addSynonyms("NoSQL", "MongoDB", "Mongo");
        // AWS synonyms
        addSynonyms("AWS", "Amazon Web Services");
        // CI/CD synonyms
        addSynonyms("CI/CD", "CICD", "CI-CD", "Continuous Integration", "Continuous Deployment");
        // Docker synonyms
        addSynonyms("Docker", "docker", "Containerization");
        // Kubernetes synonyms
        addSynonyms("Kubernetes", "K8s", "k8s");
        // REST API synonyms
        addSynonyms("REST API", "RESTful", "REST", "RESTful API");
        // Machine Learning synonyms
        addSynonyms("Machine Learning", "ML", "machine learning");
        // Artificial Intelligence synonyms
        addSynonyms("AI", "Artificial Intelligence");
        // HTML synonyms
        addSynonyms("HTML", "HTML5", "html");
        // CSS synonyms
        addSynonyms("CSS", "CSS3", "css");
        // Git synonyms
        addSynonyms("Git", "GitHub", "GitLab", "git");
        // Microservices synonyms
        addSynonyms("Microservices", "microservices", "micro-services", "Micro Services");
        // Full Stack synonyms
        addSynonyms("Full Stack", "Fullstack", "Full-Stack", "full stack", "fullstack");
        // DevOps synonyms
        addSynonyms("DevOps", "devops", "Dev-Ops", "dev ops");
        // Cloud synonyms
        addSynonyms("Cloud", "cloud computing", "Cloud Computing");
        // Azure synonyms
        addSynonyms("Azure", "Microsoft Azure", "azure");
        // Linux synonyms
        addSynonyms("Linux", "linux", "Ubuntu", "ubuntu", "CentOS");
        // Agile synonyms
        addSynonyms("Agile", "agile", "Agile Methodology");
        // Scrum synonyms
        addSynonyms("Scrum", "scrum", "Scrum Master");
        // C++ synonyms
        addSynonyms("C++", "cpp", "CPP");
        // C# synonyms
        addSynonyms("C#", "CSharp", "csharp");
        // .NET synonyms
        addSynonyms(".NET", "dotnet", "DotNet", "ASP.NET");
        // Go synonyms
        addSynonyms("Go", "Golang", "golang");
        // Rust synonyms
        addSynonyms("Rust", "rust");
        // Swift synonyms
        addSynonyms("Swift", "swift");
        // Kotlin synonyms
        addSynonyms("Kotlin", "kotlin");
        // GraphQL synonyms
        addSynonyms("GraphQL", "graphql", "Graph QL");
        // Redis synonyms
        addSynonyms("Redis", "redis");
        // Kafka synonyms
        addSynonyms("Kafka", "Apache Kafka", "kafka");
        // Jenkins synonyms
        addSynonyms("Jenkins", "jenkins");
        // Terraform synonyms
        addSynonyms("Terraform", "terraform");
        // Backend/Frontend synonyms
        addSynonyms("Backend", "backend", "back-end", "back end");
        addSynonyms("Frontend", "frontend", "front-end", "front end");
        // Data Science synonyms
        addSynonyms("Data Science", "Data Analytics", "data science", "data analytics");
        // Deep Learning synonyms
        addSynonyms("Deep Learning", "deep learning", "DL");
        // NLP synonyms
        addSynonyms("NLP", "Natural Language Processing", "nlp");
        // Selenium synonyms
        addSynonyms("Selenium", "selenium");
        // Spring Framework synonyms
        addSynonyms("Spring", "Spring Framework", "spring");
        // Hibernate synonyms
        addSynonyms("Hibernate", "hibernate");
        // RabbitMQ synonyms
        addSynonyms("RabbitMQ", "rabbitmq");
        // Elasticsearch synonyms
        addSynonyms("Elasticsearch", "elasticsearch", "Elastic Search");
    }

    private static void addSynonyms(String canonical, String... synonyms) {
        SYNONYM_MAP.put(canonical.toLowerCase(), canonical);
        for (String synonym : synonyms) {
            SYNONYM_MAP.put(synonym.toLowerCase(), canonical);
        }
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
     *
     * @param resumeText       the extracted resume text
     * @param jobDescription   the job description text
     * @return MatchResult with matched keywords, missing keywords, and match percentage
     */
    public static MatchResult match(String resumeText, String jobDescription) {
        Set<String> jdKeywords = extractKeywords(jobDescription);
        String resumeLower = resumeText.toLowerCase();

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String keyword : jdKeywords) {
            if (isKeywordInResume(keyword, resumeLower)) {
                matched.add(keyword);
            } else {
                missing.add(keyword);
            }
        }

        double percentage = jdKeywords.isEmpty() ? 0 :
                Math.round((double) matched.size() / jdKeywords.size() * 100.0 * 10.0) / 10.0;

        return new MatchResult(matched, missing, percentage);
    }

    /**
     * Extracts meaningful keywords from text (skills, technologies, tools).
     */
    private static Set<String> extractKeywords(String text) {
        // Common stop words to filter out
        Set<String> stopWords = Set.of(
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
                "when", "where", "why", "able", "etc", "e.g", "i.e",
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

        Set<String> keywords = new LinkedHashSet<>();

        // First pass: check for multi-word tech terms (e.g., "Spring Boot", "REST API", "CI/CD")
        String textLower = text.toLowerCase();
        for (Map.Entry<String, String> entry : SYNONYM_MAP.entrySet()) {
            if (textLower.contains(entry.getKey())) {
                keywords.add(entry.getValue());
            }
        }

        // Second pass: extract individual meaningful words (3+ chars, not stop words)
        String[] words = text.split("[\\s,;|/()\\[\\]{}]+");
        for (String word : words) {
            String cleaned = word.replaceAll("[^a-zA-Z0-9.#+\\-]", "").trim();
            // Strip trailing periods (but keep internal ones like "Node.js", ".NET")
            cleaned = cleaned.replaceAll("\\.$", "");
            if (cleaned.length() >= 2 && !stopWords.contains(cleaned.toLowerCase())) {
                // Check if it maps to a known synonym
                String canonical = SYNONYM_MAP.get(cleaned.toLowerCase());
                if (canonical != null) {
                    keywords.add(canonical);
                } else if (cleaned.length() >= 3 && Character.isUpperCase(cleaned.charAt(0))) {
                    // Likely a proper noun / technology name
                    keywords.add(cleaned);
                }
            }
        }

        // Third pass: remove sub-words that are parts of compound keywords
        // e.g., remove "Spring" and "Boot" if "Spring Boot" is already matched
        Set<String> toRemove = new HashSet<>();
        for (String kw : keywords) {
            if (kw.contains(" ")) {
                // This is a compound keyword - mark its individual parts for removal
                String[] parts = kw.split("\\s+");
                for (String part : parts) {
                    // Only remove if the part exists as a standalone keyword
                    // and is NOT itself a known tech term with a different canonical meaning
                    String partCanonical = SYNONYM_MAP.get(part.toLowerCase());
                    if (partCanonical != null && partCanonical.equals(part)) {
                        // The part is itself a canonical tech term — keep it
                        continue;
                    }
                    toRemove.add(part);
                }
            }
        }
        keywords.removeAll(toRemove);

        return keywords;
    }

    /**
     * Checks if a keyword (or any of its synonyms) appears in the resume text.
     */
    private static boolean isKeywordInResume(String keyword, String resumeLower) {
        // Direct match
        if (resumeLower.contains(keyword.toLowerCase())) {
            return true;
        }

        // Check all synonyms of this keyword
        String canonical = SYNONYM_MAP.getOrDefault(keyword.toLowerCase(), keyword);
        for (Map.Entry<String, String> entry : SYNONYM_MAP.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(canonical)) {
                if (resumeLower.contains(entry.getKey())) {
                    return true;
                }
            }
        }

        return false;
    }
}
