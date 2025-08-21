import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dto.OrderLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class OrderBookManager {
//    manage local order book state, parse, invoke calculations periodically.

//    TODO calculate from DataConsumer

    private final String pairName;
    private final LocalOrderBook book;
    private long lastCalculationTime = 0;
    private final Logger logger = LoggerFactory.getLogger(OrderBookManager.class);

    public OrderBookManager(String pair, int depth) {
        this.pairName = pair;
        this.book = new LocalOrderBook(pair, depth);
    }

    public String getPairName() {
        return pairName;
    }

    public LocalOrderBook getBook() {
        return this.book;
    }

    public void handleUpdateData(JsonElement data) {
        JsonArray arr = data.getAsJsonArray();
        JsonObject updateInfo = arr.get(1).getAsJsonObject();

        if (updateInfo.has("as") && updateInfo.has("bs")) {
            JsonArray asArr =  updateInfo.getAsJsonArray("as");
            JsonArray bsArr = updateInfo.getAsJsonArray("bs");
            this.onSnapshot(parseLevel(asArr), parseLevel(bsArr));
        } else {
            List<OrderLevel> askUpdates = updateInfo.has("a") ? parseLevel(updateInfo.getAsJsonArray("a"))
                    : Collections.emptyList();
            List<OrderLevel> bidUpdates = updateInfo.has("b") ? parseLevel(updateInfo.getAsJsonArray("b"))
                    : Collections.emptyList();
            this.onUpdate(askUpdates, bidUpdates);
        }
    }

    private void onSnapshot(List<OrderLevel> asksSnapshot, List<OrderLevel> bidsSnapshot) {
        TreeMap<Double, Double> asks = this.book.getAsks();
        TreeMap<Double, Double> bids = this.book.getBids();
        asks.clear();
        bids.clear();
        asksSnapshot.forEach(ask -> asks.put(ask.price(), ask.volume()));
        bidsSnapshot.forEach(bid -> bids.put(bid.price(), bid.volume()));
    }

    private void onUpdate(List<OrderLevel> askUpdates, List<OrderLevel> bidUpdates) {
        TreeMap<Double, Double> asks = this.book.getAsks();
        TreeMap<Double, Double> bids = this.book.getBids();

        for (OrderLevel a : askUpdates) {
            if (a.volume() == 0) {
                asks.remove(a.price());
            } else {
                asks.put(a.price(), a.volume());
            }
        }

        for (OrderLevel b : bidUpdates) {
            if (b.volume() == 0) {
                bids.remove(b.price());
            } else {
                bids.put(b.price(), b.volume());
            }
        }
        this.book.trim();
    }

    private List<OrderLevel> parseLevel(JsonArray array) {
        List<OrderLevel> levels = new ArrayList<>();

        for (JsonElement e : array) {
            JsonArray level = e.getAsJsonArray();
            double price = Double.parseDouble(level.get(0).getAsString());
            double volume = Double.parseDouble(level.get(1).getAsString());
            levels.add(new OrderLevel(price, volume));
        }
        return levels;
    }

    public void doCalculations() {
        if (System.currentTimeMillis() - lastCalculationTime > 1000) {
            logger.info("Mid price for {}: {}", this.pairName, this.book.getMidPrice());
            if (pairName.startsWith("XBT")) {
                logger.info("Asks VWAP for 10 BTC is {}", this.book.calculateVWAPAsks(10));
                logger.info("Bids VWAP for 10 BTC is {}", this.book.calculateVWAPBids(10));
            }
            lastCalculationTime = System.currentTimeMillis();
        }
    }
}
