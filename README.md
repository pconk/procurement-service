# Warehouse Procurement Service (Java Quarkus)

Service ini bertanggung jawab untuk mengelola data Supplier dan Purchase Order (PO) di ekosistem Warehouse System.

## 🚀 Project Status: Phase 1 (Completed)
- **CRUD Supplier:** Implementasi penuh (Create, Read, Update, Delete).
- **Architecture:** Menggunakan pola API/Domain/Infrastructure (Consistency with Golang structure).
- **Database:** MySQL dengan migrasi otomatis menggunakan Flyway.
- **Standardization:** 
    - Response Envelope: `WebResponse<T>`.
    - Pagination: `PagedResponse<T>`.
    - Global Exception Handling (System & Validation).
    - Logging AOP via `@Logged`.

## 🛠 Tech Stack
- Java 25 & Quarkus 3.34.1
- Hibernate Panache (Repository Pattern)
- MySQL 8.0
- Flyway Migration

## 📦 Cara Menjalankan

### 1. Jalankan Database (Docker)
Pastikan Docker Desktop sudah berjalan, lalu gunakan perintah:
```bash
docker-compose up -d
```

### 2. Jalankan Aplikasi (Dev Mode)
```bash
./mvnw quarkus:dev
```
Aplikasi akan berjalan di `http://localhost:8080`.

### 3. Akses Swagger UI
Dokumentasi API dapat diakses di: `http://localhost:8080/q/swagger-ui/


## 🔗 Integrasi
- **Inbound:** REST API via `wh-gateway`.
- **Outbound:** gRPC Client ke `warehouse-api` (Golang).
