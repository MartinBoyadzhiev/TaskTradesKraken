package handles;

import dto.BookUpdate;
import dto.KrakenBookUpdate;
import dto.OrderLevel;
import enums.EnvVar;
import java.util.List;
import java.util.TreeMap;

public class BookHandler {

    private final String pairName;
    private final LocalOrderBook book;

    public BookHandler(String pairName) {
        this.pairName = pairName;
        this.book = new LocalOrderBook(pairName);
    }

    public String getPairName() {
        return pairName;
    }

    public LocalOrderBook getBook() {
        return this.book;
    }

    public void handleUpdateData(BookUpdate data) {
        if (data instanceof KrakenBookUpdate update) {
            if (update.getType().equals("snapshot")) {
                this.onSnapshot(update.getAsks(), update.getBids());
            } else if (update.getType().equals("update")) {
                this.onUpdate(update.getAsks(), update.getBids());
            }
        }
    }

    private void onSnapshot(List<OrderLevel> asksSnapshot, List<OrderLevel> bidsSnapshot) {
        TreeMap<Double, Double> asks = this.book.getAsks();
        TreeMap<Double, Double> bids = this.book.getBids();
        asks.clear();
        bids.clear();
        asksSnapshot.forEach(ask -> asks.put(ask.price(), ask.qty()));
        bidsSnapshot.forEach(bid -> bids.put(bid.price(), bid.qty()));
    }

    private void onUpdate(List<OrderLevel> askUpdates, List<OrderLevel> bidUpdates) {
        TreeMap<Double, Double> asks = this.book.getAsks();
        TreeMap<Double, Double> bids = this.book.getBids();

        for (OrderLevel a : askUpdates) {
            if (a.qty() == 0) {
                asks.remove(a.price());
            } else {
                asks.put(a.price(), a.qty());
            }
        }

        for (OrderLevel b : bidUpdates) {
            if (b.qty() == 0) {
                bids.remove(b.price());
            } else {
                bids.put(b.price(), b.qty());
            }
        }
        this.trim();
    }

    private void trim() {
        TreeMap<Double, Double> asks = this.book.getAsks();
        TreeMap<Double, Double> bids = this.book.getBids();
        int depthLimit = EnvVar.DEPTH_LIMIT.getInt();

        while (asks.size() > depthLimit) {
            asks.pollLastEntry();
        }

        while (bids.size() > depthLimit) {
            bids.pollLastEntry();
        }
    }
}
