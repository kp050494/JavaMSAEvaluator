class ProductService {
    private final List<Product> products = new ArrayList<>();
    private long seq = 0;

    public Product add(String name, double price, String category) {
        Product p = new Product(++seq, name, price, category);
        products.add(p);
        return p;
    }

    public List<Product> getAll() {
        return products;
    }

    public Product findById(long id) {
        for (Product p : products) {
            if (p.id == id) return p;
        }
        return null;
    }
}
