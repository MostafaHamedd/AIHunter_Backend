-- Job Descriptions
CREATE TABLE IF NOT EXISTS job_descriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(500),
    title VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    description TEXT,
    required_skills TEXT,
    keywords TEXT,
    technologies TEXT,
    soft_skills TEXT,
    responsibilities TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Resumes
CREATE TABLE IF NOT EXISTS resumes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    summary TEXT,
    skills TEXT,
    version VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Experiences
CREATE TABLE IF NOT EXISTS experiences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    company VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    duration VARCHAR(100),
    bullets TEXT,
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
);

-- Projects
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    technologies TEXT,
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
);

-- Cover Letters
CREATE TABLE IF NOT EXISTS cover_letters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    original_content TEXT,
    optimized_content TEXT,
    version VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Job Applications
CREATE TABLE IF NOT EXISTS job_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    job_link VARCHAR(500),
    resume_id BIGINT,
    cover_letter_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_APPLIED',
    application_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE SET NULL,
    FOREIGN KEY (cover_letter_id) REFERENCES cover_letters(id) ON DELETE SET NULL
);

-- Application Notes
CREATE TABLE IF NOT EXISTS application_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES job_applications(id) ON DELETE CASCADE
);

-- Timeline Events
CREATE TABLE IF NOT EXISTS timeline_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (application_id) REFERENCES job_applications(id) ON DELETE CASCADE
);

