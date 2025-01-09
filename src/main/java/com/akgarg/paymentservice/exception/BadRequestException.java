package com.akgarg.paymentservice.exception;

import jakarta.annotation.Nullable;

import java.util.Collection;

public class BadRequestException extends RuntimeException {

    private final Collection<String> errors;

    public BadRequestException(@Nullable final Collection<String> errors) {
        super();
        this.errors = errors;
    }

    @Nullable
    public Collection<String> errors() {
        return errors;
    }

}
