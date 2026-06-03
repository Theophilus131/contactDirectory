# Contact Directory API

A production-quality **RESTful Contact Directory API** built with **Spring Boot 3.x** and **Java 17**.
It supports creating, reading, updating, deleting, and searching contacts, with full validation, structured error responses, and interactive Swagger UI documentation.

---

## Features

- Full CRUD operations on contacts
- Case-insensitive search across `firstName`, `lastName`, and `email`
- Group-based filtering (`FAMILY`, `FRIEND`, `WORK`, `OTHER`)
- Combined search + group filtering (ANDed)
- Pagination and sorting (`page`, `size`, `sort` query params)
- Email uniqueness enforcement on create and update
- `createdAt` is server-managed and immutable
- ISO-8601 timestamps (`2026-06-03T10:15:30Z`)
- Structured error responses with field-level validation details
- Two interchangeable storage backends: H2 (JPA) and pure in-memory
- Swagger UI and OpenAPI 3.0 documentation
- Sample data seeded on first startup
- Unit tests (service), slice tests (controller), and repository integration tests

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 17 |
| Framework | Spring Boot 3.2.x |
| Persistence | Spring Data JPA + H2 (or in-memory Map) |
| Mapping | MapStruct 1.5 |
| Validation | Jakarta Bean Validation (Hibernate Validator) |
| API Docs | SpringDoc OpenAPI 2.x (Swagger UI) |
| Boilerplate | Lombok |
| Build | Maven |
| Testing | JUnit 5, Mockito, MockMvc |

---

## Project Structure

```
src/main/java/com/example/contactdirectory/
├── config/
│   ├── DataSeeder.java          # Seeds sample contacts on first startup
│   └── OpenApiConfig.java       # OpenAPI metadata customisation
├── controller/
│   └── ContactController.java   # REST endpoints
├── dto/
│   ├── ContactRequest.java      # Inbound DTO with validation annotations
│   ├── ContactResponse.java     # Outbound DTO
│   └── ErrorResponse.java       # Structured error payload
├── entity/
│   ├── Contact.java             # JPA entity
│   └── ContactGroup.java        # FAMILY | FRIEND | WORK | OTHER enum
├── exception/
│   ├── ContactNotFoundException.java
│   ├── EmailAlreadyExistsException.java
│   └── GlobalExceptionHandler.java   # @ControllerAdvice
├── mapper/
│   └── ContactMapper.java       # MapStruct mapper (entity ↔ DTO)
├── repository/
│   ├── ContactRepository.java         # Spring Data JPA (h2 profile)
│   └── InMemoryContactRepository.java # ConcurrentHashMap (inmemory profile)
└── service/
    ├── ContactService.java            # Interface
    ├── JpaContactService.java         # H2-backed implementation
    └── InMemoryContactService.java    # In-memory implementation
```

---

## Build & Run

### Prerequisites

- Java 17+
- Maven 3.8+

### Build

```bash
mvn clean package -DskipTests
```

### Run with H2 database (default)

```bash
mvn spring-boot:run
```

Or using the built JAR:

```bash
java -jar target/contact-directory-api-1.0.0.jar
```

### Run with in-memory storage

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=inmemory"
```

Or with the JAR:

```bash
java -jar target/contact-directory-api-1.0.0.jar --spring.profiles.active=inmemory
```

> **Note:** In-memory mode keeps no data between restarts. H2 mode persists data in `./data/contactdb.mv.db`.

---

## Storage Profiles

| Profile | Storage | Persistence | Activate with |
|---------|---------|-------------|---------------|
| `h2` (default) | H2 file database via JPA | ✅ Survives restarts | default |
| `inmemory` | `ConcurrentHashMap` | ❌ Lost on restart | `--spring.profiles.active=inmemory` |

---

## API Endpoints

| Method | Path | Description | Success |
|--------|------|-------------|---------|
| `POST` | `/contacts` | Create a contact | 201 + Location header |
| `GET` | `/contacts` | List / search / filter | 200 |
| `GET` | `/contacts/{id}` | Get by ID | 200 |
| `PUT` | `/contacts/{id}` | Full update | 200 |
| `DELETE` | `/contacts/{id}` | Delete | 204 |

### Query Parameters for `GET /contacts`

| Parameter | Type | Description |
|-----------|------|-------------|
| `group` | enum | Filter by `FAMILY`, `FRIEND`, `WORK`, or `OTHER` |
| `search` | string | Case-insensitive match on `firstName`, `lastName`, or `email` |
| `page` | int | Zero-based page number (default: 0) |
| `size` | int | Page size (default: 20) |
| `sort` | string | Sort field(s), e.g. `sort=lastName,asc` (default: `lastName,firstName` ASC) |

---

## Swagger UI

Once the application is running, open:

```
http://localhost:8080/swagger-ui.html
```

All endpoints, parameters, request/response schemas, and error models are fully documented and tryable from the UI.

Raw OpenAPI JSON spec:

```
http://localhost:8080/api-docs
```

---

## H2 Console

Available when running the `h2` profile (default):

```
URL:      http://localhost:8080/h2-console
JDBC URL: jdbc:h2:file:./data/contactdb
Username: sa
Password: (leave blank)
```

---

## Error Response Structure

All errors return a consistent JSON payload:

```json
{
  "timestamp": "2026-06-03T10:15:30Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/contacts",
  "fieldErrors": [
    { "field": "email", "message": "Email must be a valid email address" },
    { "field": "phoneNumber", "message": "Phone number must contain digits only and be 10–15 characters long" }
  ]
}
```

| Status | Scenario |
|--------|----------|
| 201 | Contact created |
| 200 | Contact found / updated |
| 204 | Contact deleted |
| 400 | Validation failure |
| 404 | Contact not found |
| 409 | Email address already in use |

---

## Validation Rules

| Field | Rule |
|-------|------|
| `firstName` | Not blank |
| `lastName` | Not blank |
| `email` | Valid format; unique across all contacts (case-insensitive) |
| `phoneNumber` | Digits only; 10–15 characters |
| `group` | One of `FAMILY`, `FRIEND`, `WORK`, `OTHER` |

---

## Running Tests

```bash
mvn test
```

Test coverage includes:

- **Service unit tests** — mocked repository; tests uniqueness, search normalisation, immutable `createdAt`
- **Controller slice tests** — MockMvc; validates status codes, request validation, error shapes
- **Repository integration tests** — `@DataJpaTest`; tests JPQL search queries, pagination, case-insensitivity

---

## Sample curl Commands

```bash
# Create a contact
curl -s -X POST http://localhost:8080/contacts \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe","email":"jane.doe@example.com","phoneNumber":"08012345678","group":"FRIEND"}' | jq

# List all contacts
curl -s http://localhost:8080/contacts | jq

# Search by name
curl -s "http://localhost:8080/contacts?search=jane" | jq

# Filter by group
curl -s "http://localhost:8080/contacts?group=WORK" | jq

# Combined filter
curl -s "http://localhost:8080/contacts?group=FRIEND&search=jane" | jq

# Paginated, sorted
curl -s "http://localhost:8080/contacts?page=0&size=5&sort=lastName,asc" | jq

# Get by ID (replace with actual UUID)
curl -s http://localhost:8080/contacts/3fa85f64-5717-4562-b3fc-2c963f66afa6 | jq

# Update a contact
curl -s -X PUT http://localhost:8080/contacts/3fa85f64-5717-4562-b3fc-2c963f66afa6 \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Janet","lastName":"Doe","email":"janet.doe@example.com","phoneNumber":"08012345678","group":"WORK"}' | jq

# Delete a contact
curl -s -X DELETE http://localhost:8080/contacts/3fa85f64-5717-4562-b3fc-2c963f66afa6 -v
```

---

## Design Decisions

1. **UUID primary keys** — avoids sequential ID enumeration; common in modern REST APIs.
2. **Email normalisation** — emails are lowercased before storage and uniqueness checks so `Jane@Example.COM` and `jane@example.com` are treated as the same.
3. **Immutable `createdAt`** — enforced at two levels: `@Column(updatable = false)` in JPA and `ignore` in MapStruct's update mapping.
4. **Profile-based backends** — the `h2` (JPA) and `inmemory` implementations are wired via Spring profiles; the service and controller layers have no awareness of which backend is active.
5. **Dual uniqueness checks** — email uniqueness is checked at both the service layer (for a friendly 409) and the database layer (`@Column(unique = true)`) to prevent race conditions.
6. **JPQL `findByGroupAndSearch`** — a single query handles all filter combinations with `IS NULL` guards, avoiding N+1 or multiple queries.
7. **Pageable defaults** — default sort is `lastName ASC, firstName ASC` to give alphabetical listing out of the box.
8. **Sample data seeder** — active on all non-test profiles; only runs when the store is empty, so it won't re-seed on restart with the H2 file backend.
#   c o n t a c t D i r e c t o r y  
 