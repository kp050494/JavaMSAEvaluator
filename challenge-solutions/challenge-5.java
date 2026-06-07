class ResilientClient {
    public String callWithFallback(Supplier<String> upstream, String fallback, int maxAttempts) {
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                return upstream.get();
            } catch (Exception e) {
                // failed attempt - retry until attempts are exhausted
            }
        }
        return fallback;
    }
}
