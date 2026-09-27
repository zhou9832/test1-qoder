# TaskBoard

Monorepo for TaskBoard application with Spring Boot 4 backend and React 19 frontend.

## Tech Stack

- **Backend**: Java 25, Spring Boot 4.0.x, Gradle 9.1+, Kotlin DSL, H2 Database
- **Frontend**: React 19, TypeScript 5, antd 6, Vite, react-router

## Project Structure

```
├── server/          # Spring Boot backend
├── web/             # React frontend
└── README.md
```

## Quick Start

### Prerequisites
- Java 25+
- Node.js 18+
- Gradle 9.1+ (local installation at `D:\gradle-9.8.0`) or use wrapper

### Backend (server/)

```bash
cd server
# Using local Gradle installation
& "D:\gradle-9.8.0\bin\gradle.bat" bootRun    # Development mode
& "D:\gradle-9.8.0\bin\gradle.bat" build      # Build and package
# Or using wrapper (requires network to download Gradle 9.1.0)
./gradlew bootRun                            # Development mode
./gradlew build                              # Build and package
```

Backend runs on `http://localhost:8080` with API prefix `/api`.

### Frontend (web/)

```bash
cd web
npm install          # Install dependencies
npm run dev          # Development mode
npm run build        # Production build
```

Frontend runs on `http://localhost:5173` with API proxy to backend.

### Health Check

```bash
curl http://localhost:8080/api/health
```

## Development Notes

- Database: H2 in-memory (data cleared on restart)
- Schema managed via `schema.sql` and `data.sql`
- No real authentication implemented
- All HTTP responses wrapped in `ApiResponse<T>`
- All database timestamps use TIMESTAMP type with Instant in Java
