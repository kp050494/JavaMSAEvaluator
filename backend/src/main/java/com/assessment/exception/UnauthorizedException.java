package com.assessment.exception;

/** Thrown on failed authentication (bad credentials). */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
