package dto;

public class Message {
    private final String method;
    private final Params params;

    public Message(String method, Params params) {
        this.method = method;
        this.params = params;
    }
}