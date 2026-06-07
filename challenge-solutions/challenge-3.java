class ProductRepository {
    private final List<Product> data = new ArrayList<>();

    public void save(Product p) {
        data.add(p);
    }

    public List<Product> findAll() {
        return data;
    }

    public Product findById(long id) {
        for (Product p : data) {
            if (p.id == id) return p;
        }
        return null;
    }

    public List<Product> findByCategory(String category) {
        List<Product> result = new ArrayList<>();
        for (Product p : data) {
            if (p.category.equals(category)) result.add(p);
        }
        return result;
    }

    public List<Product> findByPriceLessThan(double max) {
        List<Product> result = new ArrayList<>();
        for (Product p : data) {
            if (p.price < max) result.add(p);
        }
        return result;
    }
}
