#!/bin/bash

# Alamat server gRPC Quarkus (default port 9000)
SERVER="localhost:9001"

# Token valid untuk secret: rahasia-super-aman
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NzU1NzIxNTYsImlzcyI6ImF1ZGl0LXNlcnZpY2UtdGVzdCIsInJvbGUiOiJhZG1pbiIsInVzZXJfaWQiOiIxIiwidXNlcm5hbWUiOiJhZG1pbl9ndWRhbmciLCJ3YXJlaG91c2VfaWQiOiJXSC1KS1QtMDk5In0.bsSUCiZ4B0tzGCfmzHmF6-cH1Q6WuIRQ5qgE3NdImqU"

echo "--- 1. List Services ---"
grpcurl -plaintext $SERVER list

echo -e "\n--- 2. Test ProcurementService.UpdateStatusToReceived ---"
# Menggunakan versi v1 sesuai dengan package di ProcurementGrpcService.java
grpcurl -plaintext \
  -H "X-Request-ID: req-$(date +%s)" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"po_id": 2}' \
  $SERVER com.pconk.procurement.v1.ProcurementService/UpdateStatusToReceived

echo -e "\n--- Testing Selesai ---"
