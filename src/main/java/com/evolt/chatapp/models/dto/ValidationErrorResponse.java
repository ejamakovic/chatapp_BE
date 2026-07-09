package com.evolt.chatapp.models.dto;

public class ValidationErrorResponse {

    private String field;
    private String message;

    public ValidationErrorResponse(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }
}