# Contact Directory API

A production-quality **RESTful Contact Directory API** built with **Spring Boot 3.x** and **Java 17**.

It supports creating, reading, updating, deleting, and searching contacts, with full validation, structured error responses, and interactive Swagger UI documentation.

---

## Features

* Full CRUD operations on contacts
* Case-insensitive search across `firstName`, `lastName`, and `email`
* Group-based filtering (`FAMILY`, `FRIEND`, `WORK`, `OTHER`)
* Combined search + group filtering (ANDed)
* Pagination and sorting (`page`, `size`, `sort` query params)
* Email uniqueness enforcement on create and update
* `createdAt` is server-managed and immutable
* ISO-8601 timestamps (`2026-06-03T10:15:30Z`)
* Structured error responses with field-level validation details
* Two interchangeable storage backends: H2 (JPA) and pure in-memory
* Swagger UI and OpenAPI documentation
* Sample data seeded on first startup
* Unit, controller, and repository tests

---

## Tech Stack

| Layer       | Technology                |
| ----------- | ------------------------- |
| Runtime     | Java 17                   |
| Framework   | Spring Boot 3.2.x         |
| Persistence | Spring Data JPA + H2      |
| Mapping     | MapStruct                 |
| Validation  | Jakarta Bean Validation   |
| API Docs    | Swagger UI / OpenAPI      |
| Boilerplate | Lombok                    |
| Build Tool  | Maven                     |
| Testing     | JUnit 5, Mockito, MockMvc |

---

## Project Structure

```text
src/main/java/com/example/contactdirectory/
├── config/
│   ├── DataSeeder.java
│   └── OpenApiConfig.java
├── controller/
│   └── ContactController.java
├── dto/
│   ├── ContactRequest.java
│   ├── ContactResponse.java
│   └── ErrorResponse.java
├── entity/
│   ├── Contact.java
│   └── ContactGroup.java
├── exception/
│   ├── ContactNotFoundException.java
│   ├── EmailAlreadyExistsException.java
│   └── GlobalExceptionHandler.java
├── mapper/
│   └── ContactMapper.java
├── repository/
│   ├── ContactRepository.java
│   └── InMemoryContactRepository.java
└── service/
    ├── ContactService.java
    ├── JpaContactService.java
    └── InMemoryContactService.java
```
