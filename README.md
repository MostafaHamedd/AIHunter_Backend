# HunterAI Backend

Spring Boot REST API backend for the HunterAI Job Hunting Platform.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database** (Development)
- **PostgreSQL** (Production ready)
- **Maven**

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/hunterai/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── service/         # Service interfaces
│   │   │   └── service/impl/    # Service implementations
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── pom.xml
```

## API Endpoints

### Job Descriptions
- `POST /api/job-descriptions/analyze` - Analyze job description
- `GET /api/job-descriptions/{id}` - Get job description

### Resumes
- `POST /api/resumes/upload` - Upload resume (PDF/DOCX)
- `GET /api/resumes/{id}` - Get resume
- `POST /api/resumes/{resumeId}/optimize/{jobDescriptionId}` - Optimize resume

### ATS Scoring
- `GET /api/ats/score?resumeId={id}&jobDescriptionId={id}` - Calculate ATS score

### Job Applications
- `POST /api/applications` - Create application
- `GET /api/applications` - Get all applications (with optional search/status filter)
- `GET /api/applications/{id}` - Get application
- `PUT /api/applications/{id}/status` - Update application status
- `POST /api/applications/{id}/notes` - Add note to application

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Running the Application

```bash
# Navigate to backend directory
cd backend

# Run with Maven
mvn spring-boot:run

# Or build and run
mvn clean package
java -jar target/hunter-ai-backend-1.0.0.jar
```

The API will be available at `http://localhost:8080`

### H2 Console

Access H2 database console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:hunterai`
- Username: `sa`
- Password: (empty)

## Configuration

Edit `src/main/resources/application.properties` to configure:
- Server port
- Database connection
- File upload limits
- CORS settings

## Development Notes

- Currently uses mock data for job description parsing and resume parsing
- TODO: Implement actual PDF/DOCX parsing
- TODO: Implement AI optimization service integration
- TODO: Add authentication/authorization
- TODO: Add comprehensive error handling
- TODO: Add API documentation (Swagger/OpenAPI)

## Database Schema

The application uses JPA entities with the following main tables:
- `job_descriptions` - Job postings
- `resumes` - User resumes
- `experiences` - Work experience entries
- `projects` - Project entries
- `job_applications` - Job applications
- `application_notes` - Notes on applications
- `timeline_events` - Application timeline
- `cover_letters` - Cover letters

