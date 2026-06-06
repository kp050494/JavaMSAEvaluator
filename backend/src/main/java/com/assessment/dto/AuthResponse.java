package com.assessment.dto;

/**
 * Returned from both candidate and recruiter login. {@code sessionId} is only
 * populated for candidate logins; {@code role} only for recruiter logins.
 */
public record AuthResponse(
        String token,
        String sessionId,
        String name,
        String role) {
}
