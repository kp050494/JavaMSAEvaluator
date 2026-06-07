class OrderService {
    public int reserve(int stock, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("qty must be positive");
        }
        if (qty > stock) {
            throw new IllegalStateException("insufficient stock");
        }
        return stock - qty;
    }
}
