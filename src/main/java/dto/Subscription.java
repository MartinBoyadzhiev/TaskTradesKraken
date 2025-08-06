package dto;

public record Subscription(String name, int depth) {
    public Subscription(int depth) {
        this("book", depth);
    }
}
