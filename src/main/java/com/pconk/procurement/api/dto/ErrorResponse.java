package com.pconk.procurement.api.dto;

import java.time.LocalDateTime;

public class ErrorResponse {
    public String message;
    public int status;
    public LocalDateTime timestamp;

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}