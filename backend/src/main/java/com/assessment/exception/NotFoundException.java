package com.assessment.exception;

/** Thrown when a requested entity (challenge, session, submission) does not exist. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
