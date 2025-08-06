import dto.OrderBookUpdate;
import dto.OrderLevel;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    public double getMidPrice() {
        return (bids.firstKey() + asks.firstKey()) / 2;
    }

    public double calculateVWAPAsks(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Incorrect input amount for VWAP in asks.");
        }

        double originalAmount = amount;
        double sum = 0;

        for (Map.Entry<Double, Double> entry : asks.entrySet()) {
            double price = entry.getKey();
            double volume = entry.getValue();

            if (volume >= amount) {
                sum += amount * price;
                amount = 0;
                break;
            } else {
                sum += volume * price;
                amount -= volume;
            }
        }

        if (amount > 0 ) {
            return Double.NaN;
        }

        return sum / originalAmount;
    }

    public double calculateVWAPBids(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Incorrect input amount for VWAP in bids.");
        }

        double originalAmount = amount;
        double sum = 0;

        for (Map.Entry<Double, Double> entry : bids.entrySet()) {
            double price = entry.getKey();
            double volume = entry.getValue();

            if (volume >= amount) {
                sum += amount * price;
                amount = 0;
                break;
            } else {
                sum += volume * price;
                amount -= volume;
            }
        }

        if (amount > 0 ) {
            return Double.NaN;
        }

        return sum / originalAmount;
    }

    private void trim() {

        while (asks.size() > depth) {
            asks.pollLastEntry();
        }

        while (bids.size() > depth) {
            bids.pollLastEntry();
        }
    }
}
