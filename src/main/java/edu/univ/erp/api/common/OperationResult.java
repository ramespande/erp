package edu.univ.erp.api.common;

import java.util.Optional;

public final class OperationResult<T> {
    private final boolean success;
    private final String message;
    private final T payload;

    private OperationResult(boolean success, String message, T payload) {
        this.success = success;
        this.message = message;
        this.payload = payload;
    }

    public static <T> OperationResult<T> success(T payload, String message) {
        return new OperationResult<>(true, message, payload);
    }

    public static <T> OperationResult<T> success(T payload) {
        return new OperationResult<>(true, null, payload);
    }

    public static <T> OperationResult<T> failure(String message) {
        return new OperationResult<>(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    public Optional<T> getPayload() {
        return Optional.ofNullable(payload);
    }
}

