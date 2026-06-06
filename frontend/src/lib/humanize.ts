/**
 * Turn a JUnit method name into a short readable description, e.g.
 *   "getProductById_notFound_returns404" -> "Get product by id not found returns 404"
 */
export function humanizeTestName(name: string): string {
  const cleaned = name
    .replace(/_/g, ' ')
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
    .replace(/([A-Za-z])(\d)/g, '$1 $2')
    .replace(/\s+/g, ' ')
    .trim()
    .toLowerCase();
  return cleaned.charAt(0).toUpperCase() + cleaned.slice(1);
}
