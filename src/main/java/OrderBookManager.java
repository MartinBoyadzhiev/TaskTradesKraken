import dto.OrderBookUpdate;
import dto.OrderLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OrderBookManager implements KrakenMessageHandler {

    private final Map<String, LocalOrderBook> bookManager = new ConcurrentHashMap<>();
    private final int depth;
    private final Logger logger = LoggerFactory.getLogger(OrderBookManager.class);

    public OrderBookManager(int depth) {
        this.depth = depth;
    }

    @Override
    public void onSnapshot(String pair, List<OrderLevel> asks, List<OrderLevel> bids) {
        bookManager.computeIfAbsent(pair, k -> new LocalOrderBook(depth)).initialize(asks, bids);
    }

    @Override
    public void onUpdate(String pair, OrderBookUpdate update) {
        LocalOrderBook book = bookManager.get(pair);

        if (book != null) {
            book.applyUpdate(update);
        }
    }

    @Override
    public void onError(String errorMessage) {
        logger.error("Error in WebSocket connection: {}", errorMessage);
    }

    public LocalOrderBook getBook(String pairName) {
        return this.bookManager.get(pairName);
    }

    public Set<String> getAllPairs() {
        return this.bookManager.keySet();
    }
}
