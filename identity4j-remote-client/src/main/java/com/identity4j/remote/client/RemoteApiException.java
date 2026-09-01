package com.identity4j.remote.client;

/**
 * Raised when a remote API call fails.
 */
public class RemoteApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public RemoteApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public RemoteApiException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
