package com.kinovek.backend.config;

import com.kinovek.backend.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<?> handleMissingPart(MissingServletRequestPartException ex) {
        String partName = ex.getRequestPartName();
        return ResponseEntity.badRequest().body(
                ApiResponse.error("MISSING_PARAMETER",
                        "Required parameter '" + partName + "' is missing. Please upload a resume file."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        return ResponseEntity.badRequest().body(
                ApiResponse.error("MISSING_PARAMETER",
                        "Required parameter '" + paramName + "' is missing."));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.badRequest().body(
                ApiResponse.error("FILE_TOO_LARGE",
                        "File size exceeds the maximum allowed limit of 10MB."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return ResponseEntity.internalServerError().body(
                ApiResponse.error("INTERNAL_ERROR",
                        "An unexpected error occurred. Please try again."));
    }
}
