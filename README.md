# Sentiment Widget

A full-stack feedback widget built for the WOGAA Engineering Frontend Technical Assessment.
Users submit a 1–5 rating with an optional comment. The summary panel shows live stats fetched from the backend.

---

## S3 URL of the Site 

http://sentiment-widget-with-backend-784682930082-ap-southeast-2-an.s3-website-ap-southeast-2.amazonaws.com/

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                        Browser                          │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP
          ┌──────────────▼──────────────┐
          │   React SPA (ui/)           │
          │   Vite · React 19           │
          │   Hosted on AWS S3          │
          └──────────────┬──────────────┘
                         │ REST (fetch)
          ┌──────────────▼──────────────┐
          │   Spring Boot API (backend/)│
          │   Java 21 · Spring Boot 3.5 │
          │   Hosted on AWS Elastic     │
          │   Beanstalk (EC2 t2.micro)  │
          └──────────────┬──────────────┘
                         │ JPA
          ┌──────────────▼──────────────┐
          │   H2 In-Memory Database     │
          └─────────────────────────────┘
```

---

## Repository Structure

```
├── ui/           React SPA (Vite)
└── backend/      Spring Boot REST API
```

---

## Frontend (ui/)

### Tech stack
| Concern | Choice |
|---------|--------|
| Framework | React 19 |
| Bundler | Vite 8 |
| Styling | Plain CSS with custom properties (light/dark mode) |
| Testing | Vitest + Testing Library |

### Component structure

```
src/
├── components/
│   ├── RatingChips/      1–5 rating selector chips
│   ├── CommentBox/       Optional comment textarea
│   ├── SubmitButton/     Submit trigger
│   ├── SummaryPanel/     Displays stats fetched from API
│   └── ThemeToggle/      Light / dark mode switch
├── hooks/
│   └── useFeedback.js    All widget state + API calls
├── context/
│   └── ThemeContext.jsx  Theme persistence via localStorage
└── App.jsx               Composition root
```

### Key design decisions

- **`useFeedback` hook** is the single source of state. It owns the fetch calls to the backend, the spam-prevention timer (3-second disable after submit), and all form state. Components are purely presentational.
- **`VITE_API_BASE_URL`** env variable controls where API calls go — empty in dev (Vite proxy handles it), set to the EB URL in production. The value is baked into the JS bundle at build time.
- **Vite proxy** (`/api → http://localhost:8080`) is active in dev only and stripped out of the production build automatically.
- **Theme** is driven entirely by a `data-theme` attribute on `<html>` and CSS custom properties — no JS style injection.

---

## Backend (backend/)

### Tech stack
| Concern | Choice |
|---------|--------|
| Framework | Spring Boot 3.5 |
| Language | Java 21 |
| Persistence | Spring Data JPA + H2 in-memory |
| Validation | Jakarta Bean Validation |
| API docs | Springdoc OpenAPI (Swagger UI) |

### Package structure

```
src/main/java/com/example/sentimentbackend/
├── config/
│   └── CorsConfig.java         Allowed origins via @Value injection
├── controller/
│   └── FeedbackController.java POST /api/feedback, GET /api/feedback/summary
├── service/
│   └── FeedbackService.java    Business logic, average computed in DB
├── repository/
│   └── FeedbackRepository.java JPA queries for avg rating + recent comments
├── model/
│   └── Feedback.java           Entity — id, rating, comment, createdAt
├── dto/
│   ├── FeedbackRequest.java    Validated request record
│   ├── SummaryResponse.java    Response record
│   └── RecentComment.java      Projection for recent comments
└── exception/
    └── GlobalExceptionHandler  Structured 400 on validation failure
```

### API endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/feedback` | Submit a rating (1–5) with optional comment |
| `GET` | `/api/feedback/summary` | Total submissions, average rating, 3 most recent comments |

**POST `/api/feedback` request body:**
```json
{ "rating": 4, "comment": "Great service!" }
```

**GET `/api/feedback/summary` response:**
```json
{
  "totalSubmissions": 3,
  "averageRating": "3.7",
  "recentComments": [
    { "comment": "Great service!", "rating": 4 },
    { "comment": "Could be better.", "rating": 2 }
  ]
}
```

Swagger UI is available at `/swagger-ui.html` when running locally.

### Key design decisions

- **Average and recent comments are computed by the database** (via JPQL `AVG()` and `findTop3`) rather than loading all rows and processing in memory.
- **CORS origin is injected at runtime** via the `APP_CORS_ALLOWED_ORIGIN` environment variable — no rebuild needed when the frontend URL changes.
- **`application-prod.properties`** disables the H2 console when `SPRING_PROFILES_ACTIVE=prod` is set, without duplicating any other config.

---

## Running locally

### Prerequisites
- Node.js 18+
- Java 21
- Maven (or use the included `./mvnw` wrapper)

### Backend
```bash
cd backend
./mvnw spring-boot:run
# API available at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html
# H2 console at http://localhost:8080/h2-console
```

### Frontend
```bash
cd ui
npm install
npm run dev
# App available at http://localhost:5173
```

### Running tests
```bash
cd ui
npm test
```

---

## Deployment (AWS )

### Infrastructure

| Component | AWS Service 
|-----------|-------------|
| Frontend | S3 Static Website | 
| Backend | Elastic Beanstalk (EC2 t2.micro, single instance) |
| Database | H2 in-memory | 

### Environment variables

**Elastic Beanstalk:**

| Key | Value |
|-----|-------|
| `SERVER_PORT` | `5000` |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `APP_CORS_ALLOWED_ORIGIN` |  S3 website URL |

**Frontend build (ui/.env.production):**
```
VITE_API_BASE_URL=http://your-eb-env.ap-southeast-2.elasticbeanstalk.com
```

### Deploy backend
```bash
cd backend
./mvnw clean package -DskipTests
# Upload target/sentiment-backend-0.0.1-SNAPSHOT.jar to Elastic Beanstalk
```

### Deploy frontend
```bash
cd ui
npm run build
aws s3 sync dist/ s3://your-bucket --delete
```
