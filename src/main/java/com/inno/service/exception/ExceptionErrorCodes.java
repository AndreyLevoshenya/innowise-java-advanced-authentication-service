package com.inno.service.exception;

public enum ExceptionErrorCodes {
    CREDENTIAL_NOT_FOUND("000001", "Credential with user id %d does not exist"),
    AUTHENTICATION_FAILED("000002", "Authentication failed: %s"),
    VALIDATION_EXCEPTION("000004", "Validation failed: %s"),
    METHOD_ARGUMENT_TYPE_MISMATCH_EXCEPTION("000006", "Id url path should be a number: %s");

    private final String errorCode;
    private final String errorMessage;

    ExceptionErrorCodes(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
