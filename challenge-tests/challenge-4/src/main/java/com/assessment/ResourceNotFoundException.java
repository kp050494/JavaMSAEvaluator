package com.assessment;

/**
 * Fixed domain exception thrown by the provided controller. The candidate's
 * global exception handler is expected to translate this into a 404 response
 * using the shared ErrorResponse shape. Do not modify.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
