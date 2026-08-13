# Travel Insurance Policy Issuance Platform - Phase 1

**GitHub Repository**: [travel-insurance](https://github.com/vvickyvignesh/travel-insurance)  
**Developer Profile**: [vvickyvignesh](https://github.com/vvickyvignesh)

This is the foundation (Phase 1 of 10) for the Travel Insurance Policy Issuance Platform, featuring a Java Spring Boot secure backend and a Vanilla JavaScript frontend.

## Project Structure

```text
travel-insurance-platform/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── frontend/
│   ├── index.html
│   ├── login.html
│   ├── register.html
│   ├── dashboard.html
│   ├── admin.html
│   │
│   ├── css/
│   │   └── style.css
│   │
│   └── js/
│       ├── auth.js
│       ├── api.js
│       ├── dashboard.js
│       └── admin.js
│
├── database/
│   └── schema.sql
│
├── .gitignore
└── README.md
```

## Backend Configuration

### Requirements
- **Java**: 17
- **Maven**: 3.x

### Environment Variables
Set the following environment variables prior to running the Spring Boot application:
- `DATABASE_URL`: JDBC PostgreSQL connection string (e.g. `jdbc:postgresql://<supabase-host>:5432/postgres`)
- `DATABASE_USERNAME`: Database username
- `DATABASE_PASSWORD`: Database password
- `JWT_SECRET`: A secure HS256 key of at least 256 bits (32 bytes)

## Setup instructions

1. Run the `database/schema.sql` script on your Supabase/PostgreSQL instance to create the `users` table.
2. In the `backend` directory, build the project and launch the application:
   ```bash
   mvn clean spring-boot:run
   ```
3. Open `frontend/index.html` directly in your browser or run a simple local HTTP server (e.g. VS Code Live Server at port 5500) to interact with the platform.
