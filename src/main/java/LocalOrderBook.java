import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class LocalOrderBook {
//    Store order book data, calculate, trim,

//    TODO remove trim and bring it to the manager

    private final TreeMap<Double, Double> asks = new TreeMap<>();
    private final TreeMap<Double, Double> bids = new TreeMap<>(Comparator.reverseOrder());
    private final int depth;

    public LocalOrderBook(String name, int depthLimit) {
        this.depth = depthLimit;
    }

    public TreeMap<Double, Double> getBids() {
        return bids;
    }

    public TreeMap<Double, Double> getAsks() {
        return asks;
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

    public void trim() {

        while (asks.size() > depth) {
            asks.pollLastEntry();
        }

        while (bids.size() > depth) {
            bids.pollLastEntry();
        }
    }
}
