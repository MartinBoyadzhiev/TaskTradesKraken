package dto;

import java.util.List;

public record OrderBookUpdate(List<OrderLevel> asks, List<OrderLevel> bids) {
}
