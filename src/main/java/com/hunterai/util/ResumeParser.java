package com.hunterai.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ResumeParser {
    
    public static ParsedResume parseResume(InputStream inputStream, String fileName) throws Exception {
        String text;
        
        if (fileName.toLowerCase().endsWith(".pdf")) {
            text = parsePDF(inputStream);
        } else if (fileName.toLowerCase().endsWith(".docx")) {
            text = parseDOCX(inputStream);
        } else {
            throw new IllegalArgumentException("Unsupported file format. Only PDF and DOCX are supported.");
        }
        
        return extractResumeData(text);
    }
    
    private static String parsePDF(InputStream inputStream) throws Exception {
        byte[] bytes = inputStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    private static String parseDOCX(InputStream inputStream) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
            return text.toString();
        }
    }
    
    private static ParsedResume extractResumeData(String text) {
        ParsedResume resume = new ParsedResume();
        
        // Normalize text
        String normalizedText = text.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
        String[] lines = normalizedText.split("\n");
        
        // Extract summary (usually first few lines or after "Summary", "Objective", etc.)
        resume.summary = extractSummary(lines);
        
        // Extract experience
        resume.experiences = extractExperiences(text, lines);
        
        // Extract skills
        resume.skills = extractSkills(text, lines);
        
        // Extract projects
        resume.projects = extractProjects(text, lines);
        
        return resume;
    }
    
    private static String extractSummary(String[] lines) {
        StringBuilder summary = new StringBuilder();
        boolean inSummary = false;
        int summaryLines = 0;
        
        for (int i = 0; i < Math.min(lines.length, 20); i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            // Look for summary keywords
            if (line.matches("(?i).*(summary|objective|profile|about).*")) {
                inSummary = true;
                continue;
            }
            
            if (inSummary || (i < 5 && summaryLines < 3)) {
                if (line.length() > 10 && !line.matches("^[A-Z\\s]+$")) {
                    summary.append(line).append(" ");
                    summaryLines++;
                    if (summaryLines >= 3) break;
                }
            }
        }
        
        String result = summary.toString().trim();
        return result.isEmpty() ? "Experienced professional seeking new opportunities." : result;
    }
    
    private static List<ExperienceData> extractExperiences(String text, String[] lines) {
        List<ExperienceData> experiences = new ArrayList<>();
        
        // Look for experience section
        int experienceStart = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().matches("(?i).*(experience|employment|work history|professional experience).*")) {
                experienceStart = i;
                break;
            }
        }
        
        if (experienceStart == -1) {
            // Try to find dates and company patterns
            Pattern datePattern = Pattern.compile("\\d{4}|(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{4}");
            for (int i = 0; i < lines.length; i++) {
                if (datePattern.matcher(lines[i]).find() && i < lines.length - 1) {
                    experienceStart = i;
                    break;
                }
            }
        }
        
        if (experienceStart == -1) return experiences;
        
        // Extract experience entries
        ExperienceData currentExp = null;
        List<String> currentBullets = new ArrayList<>();
        
        for (int i = experienceStart + 1; i < lines.length && experiences.size() < 10; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            // Check if this is a new experience entry (has dates and company/role)
            if (line.matches(".*\\d{4}.*") || line.matches(".*(Present|Current|Now).*")) {
                // Save previous experience
                if (currentExp != null) {
                    currentExp.bullets = new ArrayList<>(currentBullets);
                    experiences.add(currentExp);
                }
                
                // Start new experience
                currentExp = new ExperienceData();
                currentBullets.clear();
                
                // Try to extract role and company
                String[] parts = line.split("\\s+\\|\\s+|\\s+-\\s+|\\s+at\\s+");
                if (parts.length >= 2) {
                    currentExp.role = parts[0].trim();
                    currentExp.company = parts[1].trim();
                } else {
                    currentExp.role = line;
                    if (i + 1 < lines.length) {
                        currentExp.company = lines[i + 1].trim();
                        i++;
                    }
                }
                
                // Extract duration
                if (line.matches(".*\\d{4}.*")) {
                    currentExp.duration = extractDuration(line);
                }
            } else if (currentExp != null) {
                // Check if this is a bullet point
                if (line.startsWith("•") || line.startsWith("-") || line.startsWith("*") || 
                    line.matches("^\\d+\\.\\s+.*")) {
                    currentBullets.add(line.replaceFirst("^[•\\-*]\\s*|^\\d+\\.\\s*", ""));
                } else if (line.length() > 20 && !line.matches("^[A-Z\\s]+$")) {
                    // Might be a description line
                    if (currentBullets.isEmpty()) {
                        currentBullets.add(line);
                    }
                }
            }
            
            // Stop if we hit another major section
            if (line.matches("(?i).*(education|skills|projects|certifications|awards).*")) {
                break;
            }
        }
        
        // Add last experience
        if (currentExp != null) {
            currentExp.bullets = new ArrayList<>(currentBullets);
            experiences.add(currentExp);
        }
        
        return experiences;
    }
    
    private static String extractDuration(String line) {
        // Try to extract date range
        Pattern pattern = Pattern.compile("(\\d{4}|(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{4})\\s*-\\s*(\\d{4}|Present|Current|Now)");
        java.util.regex.Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }
    
    private static List<String> extractSkills(String text, String[] lines) {
        List<String> skills = new ArrayList<>();
        
        // Look for skills section
        int skillsStart = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().matches("(?i).*(skills|technical skills|technologies|tools).*")) {
                skillsStart = i;
                break;
            }
        }
        
        if (skillsStart == -1) {
            // Try to find common tech skills in the text
            String[] commonSkills = {
                "Java", "Python", "JavaScript", "TypeScript", "React", "Angular", "Vue",
                "Node.js", "Spring", "Django", "Flask", "Express", "SQL", "PostgreSQL",
                "MongoDB", "MySQL", "Redis", "Docker", "Kubernetes", "AWS", "Azure",
                "Git", "Linux", "HTML", "CSS", "REST", "GraphQL", "Microservices"
            };
            
            String lowerText = text.toLowerCase();
            for (String skill : commonSkills) {
                if (lowerText.contains(skill.toLowerCase())) {
                    skills.add(skill);
                }
            }
            return skills;
        }
        
        // Extract skills from skills section
        for (int i = skillsStart + 1; i < Math.min(lines.length, skillsStart + 20); i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            // Stop if we hit another section
            if (line.matches("(?i).*(experience|education|projects|certifications).*")) {
                break;
            }
            
            // Split by common delimiters
            String[] skillArray = line.split("[,|•\\-]");
            for (String skill : skillArray) {
                skill = skill.trim();
                if (skill.length() > 2 && skill.length() < 50) {
                    skills.add(skill);
                }
            }
        }
        
        return skills;
    }
    
    private static List<ProjectData> extractProjects(String text, String[] lines) {
        List<ProjectData> projects = new ArrayList<>();
        
        // Look for projects section
        int projectsStart = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().matches("(?i).*(projects|project|portfolio).*")) {
                projectsStart = i;
                break;
            }
        }
        
        if (projectsStart == -1) return projects;
        
        ProjectData currentProject = null;
        
        for (int i = projectsStart + 1; i < lines.length && projects.size() < 10; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            // Check if this is a new project (usually a title/name)
            if (line.length() > 5 && line.length() < 100 && 
                !line.matches(".*\\d{4}.*") && 
                !line.matches("^[•\\-*]\\s+.*")) {
                
                // Save previous project
                if (currentProject != null) {
                    projects.add(currentProject);
                }
                
                // Start new project
                currentProject = new ProjectData();
                currentProject.name = line;
                currentProject.technologies = new ArrayList<>();
            } else if (currentProject != null) {
                // Check if this is a description
                if (currentProject.description == null && line.length() > 10) {
                    currentProject.description = line.replaceFirst("^[•\\-*]\\s*", "");
                } else if (line.matches(".*(Java|Python|JavaScript|React|Node|SQL|AWS|Docker).*")) {
                    // Might be technologies
                    String[] techs = line.split("[,|•\\-]");
                    for (String tech : techs) {
                        tech = tech.trim();
                        if (tech.length() > 2 && tech.length() < 30) {
                            currentProject.technologies.add(tech);
                        }
                    }
                }
            }
            
            // Stop if we hit another major section
            if (line.matches("(?i).*(education|certifications|awards|references).*")) {
                break;
            }
        }
        
        // Add last project
        if (currentProject != null) {
            projects.add(currentProject);
        }
        
        return projects;
    }
    
    // Inner classes for parsed data
    public static class ParsedResume {
        public String summary;
        public List<ExperienceData> experiences = new ArrayList<>();
        public List<String> skills = new ArrayList<>();
        public List<ProjectData> projects = new ArrayList<>();
    }
    
    public static class ExperienceData {
        public String role;
        public String company;
        public String duration;
        public List<String> bullets = new ArrayList<>();
    }
    
    public static class ProjectData {
        public String name;
        public String description;
        public List<String> technologies = new ArrayList<>();
    }
}

