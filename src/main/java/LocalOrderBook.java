import dto.OrderBookUpdate;
import dto.OrderLevel;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class LocalOrderBook {

    private final TreeMap<Double, Double> asks = new TreeMap<>();
    private final TreeMap<Double, Double> bids = new TreeMap<>(Comparator.reverseOrder());
    private final int depth;

    public LocalOrderBook(int depthLimit) {
        this.depth = depthLimit;
    }

    public TreeMap<Double, Double> getBids() {
        return bids;
    }

    public TreeMap<Double, Double> getAsks() {
        return asks;
    }

    public void initialize(List<OrderLevel> asksSnapshot, List<OrderLevel> bidsSnapshot) {
        asks.clear();
        bids.clear();
        asksSnapshot.forEach(ask -> asks.put(ask.price(), ask.volume()));
        bidsSnapshot.forEach(bid -> bids.put(bid.price(), bid.volume()));
    }

    public void applyUpdate(OrderBookUpdate update) {

        for (OrderLevel a : update.asks()) {
            if (a.volume() == 0) {
                asks.remove(a.price());
            } else {
                asks.put(a.price(), a.volume());
            }
        }

        for (OrderLevel b : update.bids()) {
            if (b.volume() == 0) {
                bids.remove(b.price());
            } else {
                bids.put(b.price(), b.volume());
            }
        }
        trim();
    }

    private void trim() {

        while (asks.size() > depth) {
            asks.pollLastEntry();
        }

        while (bids.size() > depth) {
            bids.pollLastEntry();
        }
    }

    public double getMidPrice() {
        return (bids.firstKey() + asks.firstKey()) / 2;
    }
}
