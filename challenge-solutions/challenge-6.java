class TokenService {
    public String issue(String user, long expiresAt) {
        return user + ":" + expiresAt;
    }

    public boolean isValid(String token, long now) {
        if (token == null) return false;
        int i = token.lastIndexOf(':');
        if (i < 0) return false;
        try {
            long expiry = Long.parseLong(token.substring(i + 1));
            return now < expiry;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String subject(String token) {
        if (token == null) return null;
        int i = token.lastIndexOf(':');
        return i < 0 ? null : token.substring(0, i);
    }
}
