package handles;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class LocalOrderBook {

    private final String pairName;
    private final TreeMap<Double, Double> asks = new TreeMap<>();
    private final TreeMap<Double, Double> bids = new TreeMap<>(Comparator.reverseOrder());

    public LocalOrderBook(String pairName) {
        this.pairName = pairName;
    }

    public String getPairName() {
        return pairName;
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
}
