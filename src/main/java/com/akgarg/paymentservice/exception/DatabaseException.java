package com.akgarg.paymentservice.exception;

/**
 * @author Akhilesh Garg
 * @since 12/11/23
 */
public class DatabaseException extends RuntimeException {

    private final int errorStatusCode;

    public DatabaseException(final String message, final int errorStatusCode) {
        super(message);
        this.errorStatusCode = errorStatusCode;
    }

    public int errorStatusCode() {
        return errorStatusCode;
    }

}
