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

## 🔐 Security & Observability (JWT & Tracing)
Service ini mengimplementasikan standarisasi keamanan dan tracing yang kompatibel dengan ekosistem microservices berbasis Golang:

### 1. JWT Authentication (HMAC-SHA256)
- **Interceptor:** `HttpLoggingFilter` (REST) dan `GrpcHeaderInterceptor` (gRPC).
- **Mekanisme:** Mengekstrak header `Authorization: Bearer <token>`, memvalidasi signature menggunakan `jwt.secret`, dan memetakan claims (`user_id`, `username`, `role`) ke dalam `UserContext` yang bersifat `RequestScoped`.
- **Usage:** `UserContext` dapat di-inject ke service manapun untuk mendapatkan informasi user yang sedang login tanpa perlu parsing ulang token di layer domain.

### 2. Security Context & Tracing Propagation
- **Distributed Tracing:** Setiap request (Inbound) wajib menyertakan header `X-Request-ID`. ID ini dimasukkan ke dalam **MDC** dengan key `trace.request_id` untuk observabilitas log.
- **Propagation (Forwarding):** `OutboundInterceptor` (gRPC) secara otomatis meneruskan identitas ke service tujuan (misal: `warehouse-api` di Go). Data yang diteruskan meliputi:
    - `X-Request-ID`: Untuk menjaga rantai log antar service.
    - `Authorization`: Meneruskan JWT token asli sehingga service tujuan dapat mengenali user yang sama (Identity Propagation).

### 3. Traffic Logging
- Semua traffic masuk dicatat dalam format terstruktur di logger `http.access` (untuk REST) dan `grpc.traffic` (untuk gRPC), mencakup method, path, status code, dan durasi eksekusi dalam milidetik.

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
- gRPC (Server & Client)
- MySQL 8.0
- Flyway Migration

## 📡 gRPC Server
Service ini menjalankan gRPC Server pada port `9001` (default dev) atau sesuai konfigurasi.
- **Protobuf:** Definisi file `.proto` berada di module shared/proto.
- **Service:** `ProcurementService` menyediakan method untuk integrasi antar service.

### Cara Testing gRPC (grpcurl)
Pastikan service berjalan, lalu gunakan perintah berikut untuk melakukan update status:

```bash
grpcurl -plaintext \
  -d '{"po_id": 1}' \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -H "X-Request-ID: $(uuidgen)" \
  localhost:9001 com.pconk.procurement.v1.ProcurementService/UpdateStatusToReceived
```

**Status Code Mapping:**
- `OK (0)`: Sukses update dan trigger sinkronisasi stok.
- `NOT_FOUND (5)`: ID Purchase Order tidak ditemukan.
- `FAILED_PRECONDITION (9)`: Business logic error (misal: status sudah RECEIVED).

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
Aplikasi akan berjalan di `http://localhost:8083`.

### 3. Akses Swagger UI
Dokumentasi API dapat diakses di: `http://localhost:8083/q/swagger-ui/


## 🔗 Integrasi
- **Inbound:** REST API via `wh-gateway`.
- **Outbound:** gRPC Client ke `warehouse-api` (Golang).
