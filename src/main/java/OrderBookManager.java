import dto.OrderBookUpdate;
import dto.OrderLevel;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OrderBookManager implements KrakenMessageHandler {

    private final Map<String, LocalOrderBook> bookManager = new ConcurrentHashMap<>();
    private final int depth;

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
//  TODO
    }

    public LocalOrderBook getBook(String pairName) {
        return this.bookManager.get(pairName);
    }

    public Set<String> getAllPairs() {
        return this.bookManager.keySet();
    }
}
