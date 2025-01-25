package com.akgarg.paymentservice.exception;

/**
 * @author Akhilesh Garg
 * @since 12/11/23
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(final String message) {
        super(message);
    }

}
