package com.hunterai.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class JobDescriptionScraper {
    
    private static final int TIMEOUT = 10000; // 10 seconds
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    
    // Common job board selectors
    private static final Map<String, String[]> JOB_BOARD_SELECTORS = Map.of(
        "linkedin.com", new String[]{"h1.job-title", ".job-details__main-content", ".description__text"},
        "indeed.com", new String[]{"h2.jobTitle", "#jobDescriptionText", ".jobsearch-jobDescriptionText"},
        "glassdoor.com", new String[]{"h2.jobTitle", ".jobDescriptionContent", ".desc"},
        "monster.com", new String[]{"h1.title", "#JobDescription", ".job-details"},
        "ziprecruiter.com", new String[]{"h1.job_title", "#job_description", ".job_description"}
    );
    
    public static ScrapedJobData scrapeJobDescription(String url) {
        try {
            log.info("Scraping job description from: {}", url);
            
            // Fetch the webpage
            Document doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT)
                .followRedirects(true)
                .get();
            
            // Extract basic info
            String title = extractTitle(doc, url);
            String company = extractCompany(doc, url);
            String description = extractDescription(doc, url);
            
            // Extract structured data from description
            List<String> requiredSkills = extractSkills(description);
            List<String> technologies = extractTechnologies(description);
            List<String> keywords = extractKeywords(description);
            List<String> softSkills = extractSoftSkills(description);
            List<String> responsibilities = extractResponsibilities(description);
            
            log.info("Successfully scraped job: {} at {}", title, company);
            
            return new ScrapedJobData(
                title,
                company,
                description,
                requiredSkills,
                technologies,
                keywords,
                softSkills,
                responsibilities
            );
            
        } catch (IOException e) {
            log.error("Error scraping job description from {}: {}", url, e.getMessage(), e);
            throw new RuntimeException("Failed to scrape job description: " + e.getMessage(), e);
        }
    }
    
    private static String extractTitle(Document doc, String url) {
        // Try job board specific selectors
        String domain = getDomain(url);
        if (JOB_BOARD_SELECTORS.containsKey(domain)) {
            String[] selectors = JOB_BOARD_SELECTORS.get(domain);
            for (String selector : selectors) {
                Element element = doc.selectFirst(selector);
                if (element != null && !element.text().trim().isEmpty()) {
                    return element.text().trim();
                }
            }
        }
        
        // Fallback to common selectors
        String[] commonSelectors = {
            "h1.job-title", "h1.title", "h2.jobTitle", 
            "h1[class*='job']", "h1[class*='title']",
            "meta[property='og:title']", "title"
        };
        
        for (String selector : commonSelectors) {
            Element element = doc.selectFirst(selector);
            if (element != null) {
                String text = selector.startsWith("meta") 
                    ? element.attr("content")
                    : element.text();
                if (!text.trim().isEmpty() && text.length() < 200) {
                    return text.trim();
                }
            }
        }
        
        return "Job Position";
    }
    
    private static String extractCompany(Document doc, String url) {
        // Try job board specific selectors
        String domain = getDomain(url);
        if (JOB_BOARD_SELECTORS.containsKey(domain)) {
            String[] selectors = {
                ".company-name", ".employer", "[class*='company']",
                "a[href*='company']", ".job-company"
            };
            for (String selector : selectors) {
                Element element = doc.selectFirst(selector);
                if (element != null && !element.text().trim().isEmpty()) {
                    return element.text().trim();
                }
            }
        }
        
        // Fallback to common selectors
        String[] commonSelectors = {
            ".company", "[class*='company']", "[class*='employer']",
            "meta[property='og:site_name']"
        };
        
        for (String selector : commonSelectors) {
            Element element = doc.selectFirst(selector);
            if (element != null) {
                String text = selector.startsWith("meta")
                    ? element.attr("content")
                    : element.text();
                if (!text.trim().isEmpty() && text.length() < 100) {
                    return text.trim();
                }
            }
        }
        
        return "Company";
    }
    
    private static String extractDescription(Document doc, String url) {
        // Try job board specific selectors
        String domain = getDomain(url);
        if (JOB_BOARD_SELECTORS.containsKey(domain)) {
            String[] selectors = JOB_BOARD_SELECTORS.get(domain);
            for (int i = 1; i < selectors.length; i++) {
                Elements elements = doc.select(selectors[i]);
                if (!elements.isEmpty()) {
                    return elements.stream()
                        .map(Element::text)
                        .collect(Collectors.joining("\n\n"));
                }
            }
        }
        
        // Fallback to common selectors
        String[] commonSelectors = {
            "#jobDescriptionText", ".job-description", ".description",
            "[class*='description']", "[id*='description']",
            "main", "article", ".content"
        };
        
        for (String selector : commonSelectors) {
            Elements elements = doc.select(selector);
            if (!elements.isEmpty()) {
                String text = elements.first().text();
                if (text.length() > 100) {
                    return text;
                }
            }
        }
        
        // Last resort: get body text
        return doc.body().text();
    }
    
    private static List<String> extractSkills(String description) {
        Set<String> skills = new HashSet<>();
        String lowerDesc = description.toLowerCase();
        
        // Common technical skills
        String[] techSkills = {
            "java", "python", "javascript", "typescript", "react", "angular", "vue",
            "node.js", "spring", "django", "flask", "express", "sql", "postgresql",
            "mysql", "mongodb", "redis", "aws", "azure", "docker", "kubernetes",
            "git", "ci/cd", "rest api", "graphql", "microservices", "agile", "scrum"
        };
        
        for (String skill : techSkills) {
            if (lowerDesc.contains(skill)) {
                skills.add(capitalize(skill));
            }
        }
        
        // Extract skills from "required skills" or "qualifications" sections
        Pattern skillPattern = Pattern.compile(
            "(?:required|must have|qualifications?|skills?)[:;]?\\s*([^\\n]+(?:\\n[^\\n]+){0,5})",
            Pattern.CASE_INSENSITIVE
        );
        
        return new ArrayList<>(skills);
    }
    
    private static List<String> extractTechnologies(String description) {
        Set<String> technologies = new HashSet<>();
        String lowerDesc = description.toLowerCase();
        
        String[] techStack = {
            "react", "angular", "vue", "next.js", "nuxt", "svelte",
            "node.js", "express", "nest.js", "spring boot", "django", "flask",
            "postgresql", "mysql", "mongodb", "redis", "elasticsearch",
            "aws", "azure", "gcp", "docker", "kubernetes", "terraform",
            "jenkins", "github actions", "gitlab ci", "circleci"
        };
        
        for (String tech : techStack) {
            if (lowerDesc.contains(tech)) {
                technologies.add(capitalize(tech));
            }
        }
        
        return new ArrayList<>(technologies);
    }
    
    private static List<String> extractKeywords(String description) {
        Set<String> keywords = new HashSet<>();
        String lowerDesc = description.toLowerCase();
        
        String[] commonKeywords = {
            "full-stack", "full stack", "frontend", "front-end", "backend", "back-end",
            "full-time", "remote", "hybrid", "onsite", "on-site",
            "senior", "junior", "mid-level", "entry-level",
            "startup", "enterprise", "saas", "b2b", "b2c"
        };
        
        for (String keyword : commonKeywords) {
            if (lowerDesc.contains(keyword)) {
                keywords.add(keyword);
            }
        }
        
        return new ArrayList<>(keywords);
    }
    
    private static List<String> extractSoftSkills(String description) {
        Set<String> softSkills = new HashSet<>();
        String lowerDesc = description.toLowerCase();
        
        String[] skills = {
            "communication", "teamwork", "collaboration", "leadership",
            "problem-solving", "problem solving", "analytical", "creative",
            "time management", "organization", "adaptability", "flexibility"
        };
        
        for (String skill : skills) {
            if (lowerDesc.contains(skill)) {
                softSkills.add(capitalize(skill));
            }
        }
        
        return new ArrayList<>(softSkills);
    }
    
    private static List<String> extractResponsibilities(String description) {
        List<String> responsibilities = new ArrayList<>();
        
        // Look for bullet points or numbered lists
        Pattern bulletPattern = Pattern.compile(
            "(?:^|\\n)[•\\-*]\\s*([^\\n]+)",
            Pattern.MULTILINE
        );
        
        Pattern numberedPattern = Pattern.compile(
            "(?:^|\\n)\\d+[.)]\\s*([^\\n]+)",
            Pattern.MULTILINE
        );
        
        // Extract from "responsibilities" or "duties" sections
        Pattern sectionPattern = Pattern.compile(
            "(?:responsibilities?|duties?|what you'll do)[:;]?\\s*([^\\n]+(?:\\n[^\\n]+){0,10})",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
        );
        
        java.util.regex.Matcher matcher = bulletPattern.matcher(description);
        while (matcher.find() && responsibilities.size() < 10) {
            String resp = matcher.group(1).trim();
            if (resp.length() > 10 && resp.length() < 200) {
                responsibilities.add(resp);
            }
        }
        
        if (responsibilities.isEmpty()) {
            matcher = numberedPattern.matcher(description);
            while (matcher.find() && responsibilities.size() < 10) {
                String resp = matcher.group(1).trim();
                if (resp.length() > 10 && resp.length() < 200) {
                    responsibilities.add(resp);
                }
            }
        }
        
        return responsibilities;
    }
    
    private static String getDomain(String url) {
        try {
            java.net.URL urlObj = new java.net.URL(url);
            String host = urlObj.getHost();
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host;
        } catch (Exception e) {
            return "";
        }
    }
    
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    public static class ScrapedJobData {
        public final String title;
        public final String company;
        public final String description;
        public final List<String> requiredSkills;
        public final List<String> technologies;
        public final List<String> keywords;
        public final List<String> softSkills;
        public final List<String> responsibilities;
        
        public ScrapedJobData(String title, String company, String description,
                             List<String> requiredSkills, List<String> technologies,
                             List<String> keywords, List<String> softSkills,
                             List<String> responsibilities) {
            this.title = title;
            this.company = company;
            this.description = description;
            this.requiredSkills = requiredSkills;
            this.technologies = technologies;
            this.keywords = keywords;
            this.softSkills = softSkills;
            this.responsibilities = responsibilities;
        }
    }
}

