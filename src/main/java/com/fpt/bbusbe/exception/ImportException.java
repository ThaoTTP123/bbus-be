package com.fpt.bbusbe.exception;

import java.util.Map;

public class ImportException extends RuntimeException {
    private Map<Integer, String> errorDetails;

    public ImportException(String message, Map<Integer, String> errorDetails) {
        super(message);
        this.errorDetails = errorDetails;
    }

    public Map<Integer, String> getErrorDetails() {
        return errorDetails;
    }
}
