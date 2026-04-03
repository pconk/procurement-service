package com.pconk.procurement.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.NoArgsConstructor;

// import org.jboss.logging.Logger;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebResponse<T> {
    public int code;
    public String status;
    public String message;
    public T data;

    // private static final Logger LOG = Logger.getLogger(ValidationExceptionHandler.class);

    public WebResponse(int code, String status, String message, T data) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.data = data;

        // LOG.errorf("contructor code: %d, status: %s, message: %s", this.code, this.status, this.message);
    }

    public static <T> WebResponse<T> success(T data) {
        return new WebResponse<T>(200, "success", "Operation successful", data);
    }

    public static <T> WebResponse<T> error(int code, String status, String message) {
        // LOG.errorf("code: %d, status: %s, message: %s", code, status, message);
        return new WebResponse<T>(code, status, message, null);
    }
}