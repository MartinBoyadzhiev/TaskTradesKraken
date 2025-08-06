package dto;

import java.util.List;

public record KrakenSubscription(String event, List<String> pair, Subscription subscription) {
    public KrakenSubscription(List<String> pair, int depth) {
        this("subscribe", pair, new Subscription(depth));
    }
}
