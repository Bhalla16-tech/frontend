package com.kinovek.backend.dto;

public class ApiResponse<T> {

    private boolean success;
    private T data;
    private Object error;

    public ApiResponse() {}

    public ApiResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = new ErrorDetail(code, message);
        return response;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public Object getError() { return error; }
    public void setError(Object error) { this.error = error; }

    @SuppressWarnings("unused")
    private static class ErrorDetail {
        private String code;
        private String message;

        public ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
    }
}
