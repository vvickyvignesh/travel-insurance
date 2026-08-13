# Travel Insurance Backend - Phase 1

Spring Boot 3 REST API backend utilizing Spring Security, JWT, and Spring Data JPA.

## Package Structure
- `com.travelinsurance.config`: Spring Security and CORS configuration.
- `com.travelinsurance.controller`: Authentication, User, and Admin endpoints.
- `com.travelinsurance.dto`: Data transfer objects for request and response payloads.
- `com.travelinsurance.entity`: Persistent database entities (User table).
- `com.travelinsurance.exception`: Global exception handling.
- `com.travelinsurance.repository`: Database repositories.
- `com.travelinsurance.security`: JWT generation/verification filters.
