# Warehouse Procurement Service (Java Quarkus)

[![Procurement Service CI](https://github.com/pconk/procurement-service/actions/workflows/ci.yml/badge.svg)](https://github.com/pconk/procurement-service/actions)

Service ini bertanggung jawab untuk mengelola data Supplier dan Purchase Order (PO) di ekosistem Warehouse System.

## 🚀 Project Status

### Phase 1: Supplier Management (Completed)
- **CRUD Supplier:** Implementasi penuh (Create, Read, Update, Delete).

### Phase 2: Purchase Order Logic (Completed)
- **Master-Detail PO:** Implementasi `PurchaseOrder` dan `PurchaseOrderItem` secara atomik.
- **Business Logic:** 
    - Perhitungan otomatis `total_amount` di layer service.
    - Workflow status PO (DRAFT, PENDING, APPROVED, RECEIVED).
    - Proteksi data: PO yang sudah `APPROVED` tidak dapat diubah kembali menjadi `DRAFT`.
- **Unit Testing:** Coverage untuk `SupplierService` dan `PurchaseOrderService` menggunakan JUnit 5 dan Mockito.

## 🏗 Architecture & Standardization
- **Pattern:** API/Domain/Infrastructure (Konsisten dengan struktur microservice Golang).
- **Standardization:** Response Envelope `WebResponse<T>`, Pagination `PagedResponse<T>`, Global Exception Handling, dan Logging AOP via `@Logged`.

## 📂 Struktur Folder
Proyek ini mengikuti pola `API/Domain/Infrastructure` untuk memudahkan navigasi kode dan menjaga konsistensi dengan microservices berbasis Golang:

```text
src/main/java/com/pconk/procurement/
├── api/                # Layer Entrypoint & Data Contract
│   ├── dto/            # Request & Response objects
│   └── resource/       # REST Controllers (JAX-RS)
├── domain/             # Layer Inti Bisnis
│   ├── entity/         # Database Models (JPA)
│   └── service/        # Business Logic & Orchestration
└── infrastructure/     # Layer Teknis & Eksternal
    ├── repository/     # Database Access (Panache Repository)
    ├── grpc/           # Client gRPC untuk komunikasi ke Go
    └── config/         # Konfigurasi (MapStruct, Flyway, Filter)
```

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
