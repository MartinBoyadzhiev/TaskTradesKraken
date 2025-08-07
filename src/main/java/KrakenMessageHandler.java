import dto.OrderBookUpdate;
import dto.OrderLevel;

import java.util.List;

public interface KrakenMessageHandler {
    void onSnapshot(String pair, List<OrderLevel> asks, List<OrderLevel> bids);
    void onUpdate(String pair, OrderBookUpdate update);
    void onError(String errorMessage);
}
