class ProductValidator {
    public List<String> validate(String name, double price) {
        List<String> errors = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) {
            errors.add("name must not be blank");
        }
        if (price <= 0) {
            errors.add("price must be greater than 0");
        }
        return errors;
    }
}
